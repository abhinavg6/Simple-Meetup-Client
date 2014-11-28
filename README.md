Simple-Meetup-Client
======================

A simple Meetup client to be run on local IDE (Eclipse/IntelliJ). It connects to the Meetup API at https://api.meetup.com/, gets the groups for a topic of interest, and then gets past and upcoming events for all those groups. It then creates an output file with the group + event data, to be further used in other projects.

Please download latest version of maven to run mvn commands from command-line, or import it as a maven project in your IDE (provided maven plug-in is present). Please run "mvn clean install" and "mvn eclipse:eclipse" if you're running from a command line, and then import the project in your IDE.

Once the project is setup in IDE, you may run the class MeetupDataCollector as a Java application. To run the program, you'll need to pass two attributes - your API key which you should get from https://secure.meetup.com/meetup_api/key/, and a topic of interest like "java". The program can take a while to complete if the number of groups interested in the topic are large.
