# Deployment Guide

This section outlines step by step instructions on installing and deploying an instance of the TIM+ reference implementation server. The instructions contains steps for a select list of software platforms such as Windows, FreeBSD, Ubuntu, CentOS, and RedHat Enterprise linux.

## Assumptions
	
* User is running one of the following software platforms. Other platforms are supported and may only required slight variations of the instructions listed this section, but the TIM+ server has only been validated on the following platforms: 
  * Windows
    * Server 2016 or later
    * Windows 7 of later
  * Ubuntu Linux
    * 18.04 (Bionic Beaver)
    * 18.10 (Cosmic Cuttlefish)
  * CentOS 7.x
 
* Assumed that the install has administrative privileges on the install box.
  * Root or sudo access for linux/unix based platforms
  * Administrator privileges for Windows based platforms
  
* Assumed user has registered a domain with an accredited domain registrar such as [GoDaddy](http://www.godaddy.com/)

## General Tools and Runtimes

The reference implementation server only requires at least a Java 8 runtime be installed on the host node.

##### Java 8 SE

The Java 8 SE platform provides the runtime environment that all of the Bare Metal components will run in.

*Windows*

Download and install 8 JRE from Oracle's download web [site](https://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html).  After installing the JRE, set the JAVAHOME environment variable by following the instructions below:

* Right click on "My Computer" (may be in different locations depending on the Windows version) and select Properties
* If running later versions of Windows, you may be presented "ControlSystem" panel. If so, click Advanced system settings on the left side of the window.
* In the System Properties Dialog, click the Advanced tab and then click Environment Variables.
* Under System Variables click New.
* Use the following settings example substituting with the appropriate folder:

```
 Variable Name: JAVA_HOME
 Variable Value: C:\Program Files\java\jre1.8.0_xxx
```

Click OK on all screens.


*Ubuntu*

The Oracle JREs are supported in the WebUpdt8 Personal Package Archive (PPA) which automatically downloads and installs the JRE.

To install PPA, follow the commands below:

```
sudo add-apt-repository ppa:webupd8team/java
sudo apt-get update
```

To install Oracle Java 8, run the following command and accept the licenses.

```
sudo apt-get install oracle-java8-installer
sudo apt-get install oracle-java8-set-default
export JAVA_HOME=/usr/lib/jvm/java-8-oracle
echo "export JAVA_HOME=$JAVA_HOME" | sudo tee -a /etc/environment
```

*CentOS*

Obtain/downlaod the JRE 8 Update 202 package using the following command:

```
wget --no-cookies --no-check-certificate --header "Cookie: gpw_e24=http%3A%2F%2Fwww.oracle.com%2F; oraclelicense=accept-securebackup-cookie" https://download.oracle.com/otn-pub/java/jdk/8u202-b08/1961070e4c9b4e26a04e7f5a083f551e/jre-8u202-linux-x64.rpm
```
After downloading, execute the following commands to install the JRE

```
sudo yum localinstall jre-8u202-linux-x64.rpm 
```

Finally set the JAVAHOME environment variable for the new JDK using the following commands:

```
export JAVA_HOME=/usr/java/jre1.8.0_202-amd64
echo "export JAVA_HOME=$JAVA_HOME" | sudo tee -a /etc/environment
```

##  Obtain Reference Implementation Spring Boot Server

The TIM+ server is packaged as a single jar file which contains a monolithic Spring Boot application.  Download the latest version application fromthe  maven central repository [repository](http://repo.maven.apache.org/maven2/org/directstandards/timplus-server-boot/1.0.0-SNAPSHOT/timplus-server-boot-1.0.0-SNAPSHOT.jar).  **Note:** The maven central repository may black list some IP ranges such as virutal machines running in the Amazon EC2 cloud. Use the Sonatype repository if you are blocked from the maven central repository.

Optionally you may build the TIM+ server from source using instruction [here](https://github.com/DirectStandards/timplus-ri-build/blob/master/README.md).

Once you have downloaded or built the TIM+ server jar file, place it an appropriate location on your server node.

## Update the Default Configuration

The TIM+ server application comes with a default configuration via an embedded bootstrap.yml file which sets a default TIM+ domain name, admin console username/password, and database connection configuration.  

```
spring:
  datasource:
    url: "jdbc:hsqldb:embedded-db/openfire"
    username: sa
    password: 
        
  flyway.locations: classpath:db/migration/{vendor}  

  integration.jdbc.initializeSchema: ALWAYS
  
timplus:
  domain: domain.com
  adminUsername: timplus
  adminPassword: timplus

  monitor.condition.generalConditionTimeout: 3600000
```

This default configuration creates an TIM+ domain named 'domain.com', sets the default admin username/password to timplus/timplus, and connects to a local file hsqldb database.  **Note:** The default domain does not necessarily need to be used for TIM+ messages as it can be disabled at a later time; however the at least one domain is required to be configured in the system with an administration account associated with it.  Additional or replacement administrations can be configured in the admin console web application at a later time.

To change the default configuration, create a file named application.yml in the same directory as the timplus-server-boot jar file.  You can override the default domain, admin username/password, and database connection using the same file structure as above.  To update the database connection, you may override these setting using key/value pairs for the spring.datasource entries as outline in section [5 of the spring boot appendix](https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-application-properties.html#data-properties).

## Launch The TIM+ Server

The TIM+ server is run as a standalone Java application.  To launch the server, run the following command:

*Windows*

**NOTE:** The Windows version does not current support running as a background service.

```
java -jar direct-im-server-<version>.jar
```

*All Unix/Linux/FreeBSD*

```
java -Dworking.directory=.  -jar direct-im-server-<version>.jar > console.log 2>&1  &
```

This will write all logging to the console.log file.

## Validate Running Server

You can now validate that the server is running by accessing the admin console.  By default, the admin console runs on port 9090 and can be access (on the local server) via the following URL:  http://localhost:9090/

Login using the default username/password configured in your application.yml file.  At this point you are ready to continue configuring your TIM+ implementation.  Refer to the [Configuration](Configuration) guide for instructions on completing the configuration.
