package edu.mayo.cim.sianotifier;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import edu.mayo.cim.sianotifier.sia.AssayDefinition;
import edu.mayo.cim.sianotifier.sia.Contact;
import edu.mayo.cim.sianotifier.sia.PanelDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.time.Period;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootApplication
public class SiaNotificationApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(SiaNotificationApplication.class);

    @Value("${archiveNotification.emailTemplate.ctd}")
    private String ctdArchiveNotificationTemplate;

    @Value("${archiveNotification.emailTemplate.prod}")
    private String prodArchiveNotificationTemplate;

    @Value("${archiveNotification.warningPeriodBeforeCleanup:P0Y2M0D}")
    private String periodBeforeCleanupToSendNotifications = "P0Y2M0D";

    @Value("${archiveNotification.cleanupPeriod:P0Y6M0D}")
    private String periodAfterGoLiveToStartCleanup = "P0Y6M0D";

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private SiaPanelService siaPanelService;

    @Autowired
    private NotificationDao notificationDao;

    /** by default we don't want to accidently spam people */
    @Value("${archiveNotification.emailTemplate.mockSend:true}")
    private boolean mockSend = true;

    @Value("${archiveNotification.emailTemplate.isHtml:false}")
    private boolean htmlMessage = false;

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(SiaNotificationApplication.class, args);
        // close down the application, this will shut down all beans including, importantly the notificationDao which will be persisted.
        run.close();
        System.exit(0);
    }

    @Override
    public void run(String... args) throws Exception {
        List<AssayDefinition> assays = siaPanelService.listAllAssays();

        //make the panel definition the primary element we want to deal with, make sure it has details from assay that we need.
        assays.forEach(ad -> {
            Set<Contact> notificationContacts = assembleNotificationContacts(ad);
            ad.getPanelDefinitions().forEach(pd -> {
                pd.setProjectNumber(ad.getProjectNumber());
                pd.setNotificationContacts(notificationContacts);
            });
        });

        List<PanelDefinition> panelsToNotifyAbout = getPanelsToNotifyAbout(assays);

        panelsToNotifyAbout.stream()
                .map(pd -> this.produceNotificationMessage(pd, prodArchiveNotificationTemplate))
                .forEach(this::sendMessage);

        assembleMessageForFirstForAGivenAssay(panelsToNotifyAbout)
                .forEach(this::sendMessage);
    }

    private Stream<SimpleMailMessage> assembleMessageForFirstForAGivenAssay(List<PanelDefinition> panelsToNotifyAbout) {
        return panelsToNotifyAbout.stream()
                .filter(pd -> !notificationDao.hasBeenNotified(pd.getProjectNumber(), null))
                //get only one (the earliest) for assays that have not been notified yet
                .collect(Collectors.toMap(PanelDefinition::getProjectNumber, pd -> pd, (first, second) -> {
                    return (first.getGoLiveDate().isBefore(second.getGoLiveDate()) ? first : second);
                })).values().stream()
                .map(pd -> this.produceNotificationMessage(pd, ctdArchiveNotificationTemplate));
    }

    private void sendMessage(SimpleMailMessage simpleMailMessage) {
        if(simpleMailMessage == null){
            logger.warn("We have a null email message for some reason.");
            return;
        }

        logger.info("Sending Message: {}", simpleMailMessage);
        if (this.htmlMessage) {
            //turn the simple message into a mime message, wrapping in html
            logger.debug("Sending HTML...");
            mailSender.send(mimeMessage -> {
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
                helper.setFrom(simpleMailMessage.getFrom());
                helper.setSubject(simpleMailMessage.getSubject());
                helper.setTo(simpleMailMessage.getTo());
                helper.setText(wrapHtmlBody(simpleMailMessage.getText()), true);
            });
        } else {
            mailSender.send(simpleMailMessage);
        }
    }

    private static String wrapHtmlBody(String text) {
        return String.format("<!DOCTYPE html lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\"><html><body>%s</body></html>", text);
    }

    private Stream<SimpleMailMessage> assembleNotificationsOfAssaysSoonEligableForCTDCleanup(List<PanelDefinition> panelsToNotifyAbout) {
        return null;
    }

    private Set<Contact> assembleNotificationContacts(AssayDefinition assay) {
        Set<Contact> contacts = new HashSet<>();
        if (mockSend) {
            contacts.add(new Contact().setEmail("Winter.Eric@mayo.edu"));
        } else {
            //TODO: which contacts should get this notification?
            contacts.addAll(assay.getHostLabContacts());
            contacts.addAll(assay.getCgslDevTechs());
        }
        return contacts;
    }

    private SimpleMailMessage produceNotificationMessage(PanelDefinition panelDefinition, String template) {
        TemplatedEmailMessage message = createTemplateEmail(template);

        message.setVariable("ordered_service", "")
                .setVariable("ngs_number", panelDefinition.getProjectNumber())
                .setVariable("project_name", panelDefinition.getMnemonic())
                .setVariable("archive_date", DateTimeFormatter.ofPattern("dd MMM yyyy").format(panelDefinition.getGoLiveDate().plus(Period.parse(periodAfterGoLiveToStartCleanup))));

        panelDefinition.getNotificationContacts().forEach(contact -> message.addRecipient(contact.getEmail()));
        return message.getMessage();
    }

    private TemplatedEmailMessage createTemplateEmail(String template) {
        List<String> lineSplit = Splitter.on('\n').splitToList(template.replaceAll("\\^", "\\$"));
        return new TemplatedEmailMessage(lineSplit.get(0), Joiner.on('\n').join(lineSplit.subList(1, lineSplit.size())), "SIA-DNR@mayo.edu");
    }

    private List<PanelDefinition> getPanelsToNotifyAbout(List<AssayDefinition> assays) {
        ZonedDateTime now = ZonedDateTime.now();
        return assays.stream()
                .flatMap(ad -> ad.getPanelDefinitions().stream())
                .filter(pd -> pd.getPhase() != null)
                .filter(pd -> pd.getGoLiveDate() != null)
                .filter(panelDefinition -> !notificationDao.hasBeenNotified(panelDefinition.getProjectNumber(), panelDefinition.getMnemonic()))
                .filter(pd -> "Clinical".equals(pd.getPhase()))
                .filter(pd -> {
                    ZonedDateTime serviceStartArchiveDate = pd.getGoLiveDate().plus(Period.parse(periodAfterGoLiveToStartCleanup));
                    boolean hasNotYetStartedToArchive = serviceStartArchiveDate.isAfter(now);
                    boolean isAfterWhenWeShouldSendNotification = now.isAfter(serviceStartArchiveDate.minus(Period.parse(periodBeforeCleanupToSendNotifications)));
                    return hasNotYetStartedToArchive && isAfterWhenWeShouldSendNotification;
                })
                .collect(Collectors.toList());
    }


}
