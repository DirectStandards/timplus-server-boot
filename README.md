# Overview

The TIM+ server is a monolithic Spring Boot application which encapsulates a highly modified version of the Openfire XMPP Server.  The application contains both the core XMPP server that implements the TIM+ specification as well as a configuration web application that includes:

* Creating TIM+ domains
* Managing certificate and trust anchors
* Creating and managing users
* Limited operational monitoring  

This TIM+ Spring Boot server application is not intended to be full blown, production ready implementation. Instead it is a starting point from which a production system can be derived and deployed.


## Guides

This document describes the installation of the TIM+ server and steps for configuring the TIM+ implementation. 

* [Deployment Guide](DepGuide) - This section describes how to deploy the TIM+ server.
* [Deployment Configuration](Configuration) - This section describes how to configure TIM+ service and perform operations such as configuring DNS, installing certificates, creating domains, and creating users.
