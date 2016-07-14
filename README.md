# SmartMuseum

![alt tag](https://github.com/andr3aranieri/smartmuseumimages/blob/master/app_icon.png)

##Introduction

Smart Museum is a pervasive Android application that enhances user’s museum Experience. 
Using bluetooth low energy, the application is able to sense the physical environment and adapt its behavior detecting the exhibits around the user and estimating their distance from him.
Using a graph nosql database, the app stores every action of the user inside the museum, to be able to create a complete user history of visits.
The user will be able to 
- receive detailed informations about the exhibits around him directly on his smartphone;
- access his history of visits whenever, wherever you want; 
- interact with museum experts to clarify every doubt or to obtain more detailed informations about the exhibits;

The interaction with the physical environment is implemented using eddystone beacons:
- Monitoring: when the user approaches one of the museum entrances, he will receive a notification that invites him to start the app and to enter the museum;
- Ranging: when the user is inside the museum, the app detectes the exhibits around him showing them ordered by distance in real time;

The ask to an expert function is implemented using the http://smartmuseumask.slack.com/ Slack team. 
The Slack integration is done using a BOT token; it is transparent to the user (the user doesn’t use his Slack account), and in the future could be replaced by a proprietary message server.

##Technologies

- Estimote Android SDK used for beacons monitoring and ranging: http://estimote.com/
- Neo4j hosted on Amazon EC2 ubuntu machine for data storing: https://neo4j.com/
- Amazon AWS EC2: https://aws.amazon.com/ec2/?nc1=h_ls
- Amazon S3 cloud storage for images store: https://aws.amazon.com/s3/
- Square Retrofit Android library for interacting with Neo4j RESTful API: http://square.github.io/retrofit/
- Square Picasso Android library to manage images download and caching: http://square.github.io/picasso/

##Architecture

![alt tag](https://github.com/andr3aranieri/smartmuseumimages/blob/master/architecture.png)

The Estimote Android SDK lets the app interact with beacons (detecting the user entering a beacon region, the list of beacons around him and estimating the distances):
![alt tag](https://github.com/andr3aranieri/smartmuseumimages/blob/master/arch_beacons.png)

The graph nosql dbms Neo4J, installed on a ubuntu virtual machine hosted by amazon EC2, stores the database.

######Museum Structure
![alt tag](https://github.com/andr3aranieri/smartmuseumimages/blob/master/arch_neo4j1.png)

######User Visits store, using a time tree to access to them chronologically
![alt tag](https://github.com/andr3aranieri/smartmuseumimages/blob/master/arch_neo4j2.png)

The amazon S3 bucket is used to store and retrieve the multimedia files (audio, images, long text files).

https://smartmuseumask.slack.com Slack team lets the user interact with museum experts. We use a set of pre-created channels. The number of channels depends on the estimated number of users. 
When the user registers to the application, the system chooses the first free channel and permanently assigns it to him. The user will use this channel to interact with museum experts.

######User Registration
![alt tag](https://github.com/andr3aranieri/smartmuseumimages/blob/master/arch_userregistration.png)

######User - Expert interaction
![alt tag](https://github.com/andr3aranieri/smartmuseumimages/blob/master/arch_userexpertinteraction.png)


##Application Screenshots


##Installation instructions

1. Download and install android studio
2. Clone the https://github.com/smartmuseumandroidapp/SmartMuseum git repository
3. Join https://smartmuseumask.slack.com team.



