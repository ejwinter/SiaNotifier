package edu.mayo.cim.sianotifier;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Version;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.IOException;
import java.util.*;

/**
 * A wrapper for having an email with template variables that can be filled in.  This uses
 * FreeMarker 2.3 as a templating engine.
 */
public class TemplatedEmailMessage {

    public static final Configuration TEMPLATE_CONFIGURATION = new Configuration(new Version(2, 3, 23));

    private final Template messageTemplate;

    private final Template subjectTemplate;

    private final String from;

    private final Map<String,String> variables = new HashMap<>();

    private final List<String> recipients = new LinkedList<>();

    public TemplatedEmailMessage(String subjectTemplate, String messageTemplate, String from) {
        try {
            this.messageTemplate = new Template("messageTemplate", messageTemplate, TEMPLATE_CONFIGURATION);
            this.subjectTemplate = new Template("subjectTemplate", subjectTemplate, TEMPLATE_CONFIGURATION);
        } catch (IOException e) {
            throw new RuntimeException("We could not create message templates.", e);
        }

        this.from = from;
    }

    public SimpleMailMessage getMessage(){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipients.toArray(new String[]{}));
        message.setFrom(from);
        try {
            message.setText(FreeMarkerTemplateUtils.processTemplateIntoString(messageTemplate, variables));
            message.setSubject(FreeMarkerTemplateUtils.processTemplateIntoString(subjectTemplate, variables));
            return message;
        } catch (IOException | TemplateException e) {
            throw new RuntimeException(e);
        }
    }

    public TemplatedEmailMessage setVariable(String name, String value){
        this.variables.put(name,value);
        return this;
    }

    public void addRecipient(String ... recipient){
        this.recipients.addAll(Arrays.asList(recipient));
    }

    public void removeRecipient(String ... toRemove){
        Set<String> toRemoveSet = new HashSet<>(Arrays.asList(toRemove));
        this.recipients.removeIf(toRemoveSet::contains);
    }
}
