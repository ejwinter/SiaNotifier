# Sample Intake Application (SIA) Notifier

## Introduction

The project was a quick project to be used to send notification messages
to users of SIA.  It is likely not useful beyond that besides as documentation
on how to have a simple command line Spring Boot application that is a client
for a basic REST interface to get data.

The basic steps it performs include:
1. Ask SIA for a list of all assays.
2. Find out all panels defined in those assays that are live and will soon begin to have the data archived.
3. For those panels assemble a list of contacts and send them a notification of the pending archive starting.
4. Assemble emails to send based on a templated email.
  * prod emails are sent each time a panel goes live.
  * ctd emails are sent once when the first panel goes live for a given assay.
5. Send email.  Making note of which ones we are sending so we do not send again.

## Usage

You can build this tool using maven <code>mvn package</code>.

This will create a target/sia-notifier.jar file that includes all dependencies.

You then can deploy.  

1. Create a empty text file to act as your repository of sent notifications.
2. Create a application.yml and starting from the one discussed below.

You can then invoke like this:

<code>

% java -Dspring.config.location=application.yml -jar sia-notifier.jar
</code>

## Configuration

All configuration occurs within the application.yml that is presented to
the application via -Dspring.config.location=/path/to/application.yml.
There is a default on in src/main/resources but many of the values should
be overwritten with the configuration presented.

Reference the default [application.yml](./src/main/resources/application.yml) an
explanation of the configuration. 


  * archiveNotification
    * notificationRepository - is the path to a file where we keep track of which projectNumbers and "projectNumbers-mnemonic"
      combinations we have already sent notifications to.
    * cleanupPeriod - is how long after go live that a project is subject to cleanup.  The format 
      is P1Y2M3D for 1 year, 2 months, and 3 days.
    * warningPeriodBeforeCleanup - is how long before cleanup will start that we should send notification.  The format 
      is P1Y2M3D for 1 year, 2 months, and 3 days.
    * emailTemplate - these settings indicate what email content should be sent as part of notifications
      * isHtml (default: false) should we ensure the email sent is html and not just plain text.  We will wrap it in proper
        html.
      * carbonCopy is an optional email address we should CC on all messages.
      * mockReceiver is an optional email address we should use instead of all designated contacts.
      * ctd is a template for the email to send when we notify users of soon to begin CTD cleanup.  We only 
        let users know of the first to go live CTD panel for a given project.
      * prod is a template for the email to send when we notify users of a soon to begin PROD cleanup.  All
        panels get a production notification based on their go live.
  * sia
    * url (see below, a bit more detail needed)
  * spring these settings are for spring boot.  You can see descriptions elsewhere.
    * main
      * web-application-type: none indicates that we do not launch as web app.  This is needed in this case.
    * mail these settings are for the email server to use.  Please reference Spring Boot Mail for further details.
    * logging these are likewise definedby Spring Boot documentation.
   

### sia.url

This is the response expect to see when we do a GET request to the sia.url

This has been narrowed down to the pieces we actually use.

<code json>

    [
      {
        "projectNumber" : "NGS02",
        "proponents" : [ {
          "email" : "Winter.Eric@mayo.edu"
        }, {
          "email" : "Winter.Eric@mayo.edu"
        } ],
        "hostLabContacts" : [ {
          "email" : "Winter.Eric@mayo.edu"
        }, {
          "email" : "Winter.Eric@mayo.edu"
        } ],
        "informaticsSpecialists" : [ {
          "email" : "Winter.Eric@mayo.edu"
        }, {
          "email" : "Winter.Eric@mayo.edu"
        } ],
        "panelDefinitions" : [ {
          "mnemonic" : "HCRC",
          "goLiveDate" : "2018-01-01T06:00:00Z",
          "phase" : "Verification"
        }, {
          "mnemonic" : "APCZ",
          "goLiveDate" : "2018-08-01T05:00:00Z",
          "phase" : "Clinical"
        }, {
          "mnemonic" : "MYHZ"
          "goLiveDate" : null,
          "phase" : "Verification"
        } ]
      }
    ]
</code>

