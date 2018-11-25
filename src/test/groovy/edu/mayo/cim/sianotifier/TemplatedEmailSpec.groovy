package edu.mayo.cim.sianotifier


import org.springframework.mail.SimpleMailMessage
import spock.lang.Specification
import spock.lang.Subject

class TemplatedEmailSpec extends Specification {

    @Subject
    TemplatedEmailMessage templatedEmail

    def "createsMessage"(){
        given:
        templatedEmail = new TemplatedEmailMessage('subject ${name}', 'message ${fname} ${lname}', "Winte@mayo.edu")
            .setVariable("name", "joe")
            .setVariable("lname", "winter")
            .setVariable("fname", "eric")

        when:
        SimpleMailMessage message = templatedEmail.getMessage()


        then:
        message.getFrom() == "Winte@mayo.edu"
        message.getText() == "message eric winter"
        message.getSubject() == "subject joe"
    }
}
