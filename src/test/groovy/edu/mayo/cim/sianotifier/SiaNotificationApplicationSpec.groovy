package edu.mayo.cim.sianotifier

import edu.mayo.cim.sianotifier.sia.AssayDefinition
import edu.mayo.cim.sianotifier.sia.Contact
import edu.mayo.cim.sianotifier.sia.PanelDefinition
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessagePreparator
import spock.lang.Specification
import spock.lang.Subject

import java.time.ZonedDateTime
import java.util.stream.Collectors

class SiaNotificationApplicationSpec extends Specification {

    @Subject
    SiaNotificationApplication application

    def setup() {

        application = new SiaNotificationApplication()

        application.notificationDao = Mock(NotificationDao)
        application.siaPanelService = Mock(SiaPanelService)
        application.mailSender = Mock(JavaMailSender)
        application.prodArchiveNotificationTemplate = "basic prod subject ^{ngs_number}-^{project_name}\nbasic prod text ^{ngs_number}-^{project_name}"
        application.ctdArchiveNotificationTemplate = "basic ctd subject ^{ngs_number}\nbasic ctd text ^{ngs_number}"
    }


    def "sendMessage html"() {
        given:
        SimpleMailMessage message = new SimpleMailMessage()
        and:
        application.htmlMessage = true
        when:
        application.sendMessage(message)
        then:
        1 * application.mailSender.send(_ as MimeMessagePreparator)
    }

    def "sendMessage plain"() {
        given:
        SimpleMailMessage message = new SimpleMailMessage()
        when:
        application.sendMessage(message)
        then:
        1 * application.mailSender.send(message)
    }

    def "assembleNotificationContacts"() {
        given:
        AssayDefinition assay = new AssayDefinition(cgslDevTechs: [new Contact(email: 'one@mayo.edu')], hostLabContacts: [new Contact(email: 'two@mayo.edu'), new Contact(email: 'three@mayo.edu')])
        and:
        application.mockSend = false

        when:
        Set<Contact> contactList = application.assembleNotificationContacts(assay)

        then:
        contactList.size() == 3
        contactList.collect { it.email }.containsAll(['one@mayo.edu', 'two@mayo.edu', 'three@mayo.edu'])
    }

    def "getPanelsToNotifyAbout"() {
        ZonedDateTime now = ZonedDateTime.now()

        given:
        AssayDefinition shouldGetSent = new AssayDefinition(projectNumber: 'NGS01', panelDefinitions: [new PanelDefinition(mnemonic: 'p1', phase: 'Clinical', goLiveDate: now.minusMonths(5L))])
        AssayDefinition notReadyForNotification = new AssayDefinition(projectNumber: 'NGS02', panelDefinitions: [new PanelDefinition(mnemonic: 'p2', phase: 'Clinical', goLiveDate: now.minusMonths(3L))])
        AssayDefinition alreadyShouldHaveStartedArchiving = new AssayDefinition(projectNumber: 'NGS03', panelDefinitions: [new PanelDefinition(mnemonic: 'p3', phase: 'Clinical', goLiveDate: now.minusMonths(7L))])
        AssayDefinition noArchiveDateSet = new AssayDefinition(projectNumber: 'NGS04', panelDefinitions: [new PanelDefinition(mnemonic: 'p4', phase: 'Clinical')])
        AssayDefinition noPhaseSet = new AssayDefinition(projectNumber: 'NGS05', panelDefinitions: [new PanelDefinition(mnemonic: 'p5', goLiveDate: now.minusMonths(5L))])
        AssayDefinition alreadyNotified = new AssayDefinition(projectNumber: 'NGS06', panelDefinitions: [new PanelDefinition(mnemonic: 'p6', phase: 'Clinical', goLiveDate: now.minusMonths(5L))])
        AssayDefinition notCorrectPhase = new AssayDefinition(projectNumber: 'NGS07', panelDefinitions: [new PanelDefinition(mnemonic: 'p7', phase: 'Other', goLiveDate: now.minusMonths(5L))])
        and:
        List<AssayDefinition> assays = [
                shouldGetSent, notReadyForNotification, alreadyShouldHaveStartedArchiving, noArchiveDateSet, noPhaseSet, alreadyNotified, notCorrectPhase
        ].each { ad -> ad.panelDefinitions.each { pd -> pd.setProjectNumber(ad.projectNumber) } }

        and:
        application.notificationDao.hasBeenNotified("NGS06", "p6") >> true

        when:
        List<PanelDefinition> panels = application.getPanelsToNotifyAbout(assays)

        then:
        panels.size() == 1
        panels.collect { it.mnemonic } == ['p1']

    }

    def "assembleNotificationsOfAssaysSoonEligableForCTDCleanup"() {

        ZonedDateTime now = ZonedDateTime.now()
        given:
        PanelDefinition p1 = new PanelDefinition(projectNumber: "NGS01", mnemonic: "p1", goLiveDate: now, notificationContacts: [new Contact(email: "me@mayo.edu")])
        PanelDefinition alreadyNotified = new PanelDefinition(projectNumber: "NGS02", mnemonic: "p2", goLiveDate: now, notificationContacts: [new Contact(email: "me@mayo.edu")])
        PanelDefinition samePanelAsP1LiveLater = new PanelDefinition(projectNumber: "NGS01", mnemonic: "p3", goLiveDate: now.plusMonths(1L), notificationContacts: [new Contact(email: "me@mayo.edu")])
        PanelDefinition samePanelAsP1LiveEariler = new PanelDefinition(projectNumber: "NGS01", mnemonic: "p4", goLiveDate: now.minusDays(1L), notificationContacts: [new Contact(email: "me@mayo.edu")])

        List<PanelDefinition> panels = [p1, alreadyNotified, samePanelAsP1LiveLater, samePanelAsP1LiveEariler]

        and:
        application.notificationDao.hasBeenNotified("NGS02", null) >> true
        application.notificationDao.hasBeenNotified("NGS01", null) >> false

        when:
        List<SimpleMailMessage> messages = application.assembleMessageForFirstForAGivenAssay(panels).collect(Collectors.toList())

        then:
        messages.size() == 1
        messages[0].text == "basic ctd text NGS01"

    }

    def "run"() {

        ZonedDateTime now = ZonedDateTime.now()

        given:
        AssayDefinition shouldGetSent = new AssayDefinition(projectNumber: 'shouldGetSent', panelDefinitions: [new PanelDefinition(mnemonic: 'p1', phase: 'Clinical', goLiveDate: now.minusMonths(5L))])
        AssayDefinition notReadyForNotification = new AssayDefinition(projectNumber: 'notReadyForNotification', panelDefinitions: [new PanelDefinition(mnemonic: 'p2', phase: 'Clinical', goLiveDate: now.minusMonths(3L))])
        AssayDefinition alreadyShouldHaveStartedArchiving = new AssayDefinition(projectNumber: 'alreadyShouldHaveStartedArchiving', panelDefinitions: [new PanelDefinition(mnemonic: 'p3', phase: 'Clinical', goLiveDate: now.minusMonths(7L))])
        AssayDefinition noArchiveDateSet = new AssayDefinition(projectNumber: 'noArchiveDateSet', panelDefinitions: [new PanelDefinition(mnemonic: 'p4', phase: 'Clinical')])
        AssayDefinition noPhaseSet = new AssayDefinition(projectNumber: 'noPhaseSet', panelDefinitions: [new PanelDefinition(mnemonic: 'p5', goLiveDate: now.minusMonths(5L))])
        AssayDefinition alreadyNotified = new AssayDefinition(projectNumber: 'alreadyNotified', panelDefinitions: [new PanelDefinition(mnemonic: 'p6', phase: 'Clinical', goLiveDate: now.minusMonths(5L))])
        AssayDefinition notCorrectPhase = new AssayDefinition(projectNumber: 'notCorrectPhase', panelDefinitions: [new PanelDefinition(mnemonic: 'p7', phase: 'Other', goLiveDate: now.minusMonths(5L))])
        and:
        application.siaPanelService.listAllAssays() >> [
                shouldGetSent, notReadyForNotification, alreadyShouldHaveStartedArchiving, noArchiveDateSet, noPhaseSet, alreadyNotified, notCorrectPhase
        ]
        and:
        application.notificationDao.hasBeenNotified("alreadyNotified", null) >> true
        application.notificationDao.hasBeenNotified("alreadyNotified", "p6") >> true

        when:
        application.run()

        then:
        2 * application.mailSender.send(_)
    }
}