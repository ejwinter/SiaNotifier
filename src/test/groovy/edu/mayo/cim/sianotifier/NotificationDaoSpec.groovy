package edu.mayo.cim.sianotifier

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Subject

import java.nio.file.Path

class NotificationDaoSpec extends Specification {
    @Subject
    NotificationDao notificationDao

    @Rule
    TemporaryFolder temporaryFolder

    Path daoFile

    def setup(){
        daoFile = temporaryFolder.newFile().toPath()
        notificationDao = new NotificationDao(daoFile.toAbsolutePath().toString())
    }

    def "setup the dao and then persist it, load again and maintain state."(){

        when:
        notificationDao.registerNotification("NGS01", "mnemonic")
        notificationDao.close()

        and: "We load a new dao from the saved file"
        NotificationDao newDao = new NotificationDao(daoFile.toAbsolutePath().toString())
        boolean isRegistered = notificationDao.hasBeenNotified("NGS01", "mnemonic")
        boolean isNotRegistered = notificationDao.hasBeenNotified("NGS01", "nope")

        then:
        isRegistered == true
        isNotRegistered == false



    }

}
