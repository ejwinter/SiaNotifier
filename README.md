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
5. Send email.  Making note of which ones we are sending so we do not send again.

## Configuration

All configuration occurs within the application.yml that is presented to
the application via -Dspring.config.location=/path/to/application.yml.
There is a default on in src/main/resources but many of the values should
be overwritten with the configuration presented.

Reference the default [application.yml](./src/main/resources/application.yml) an
explanation of the configuration.

