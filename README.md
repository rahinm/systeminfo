SystemInfo
==========

SystemInfo is a collection of simple tools for help managing a JEE application server.

SystemInfo uses a number of managed beans for the Java platform. These are obtained via the Java
class ManagementFactory. Utilising a set of available management beans SystemInfo provides information 
on JVM's current status or metrics on classloading stats, memory usage, garbage collections stats,
threads etc. These stats or metrics obtained are a snapshot of the state of the JVM at the moment
the snapshot is taken. SystemInfo does not gather and store stats/metrics continuously and as such
is not (& does not asipire to be) an application performance monitoring (APM) system. Where APM 
functionalities are required please consider APM products like,

Commercial:
 * AppDynamics
 * NewRelic
 * DynaTrace etc.

Open Source:
 * PinPoint
 * InspectIT
 * Stagemonitor etc.

SystemInfo is pacakged in a war file that can be deployed into any JEE application server that
supports Servlet specification version 2.5 or above. It is designed as lightweight as possible
and also as least intrusive as possible. The processing overhead is kept to a bere minimum. Currently
the following set of information are available,

 * Host Information
 * JVM Information
 * System Properties
 * Class Loading Information
 * Memory Information
 * Garbage Collection Information
 * Threads Information (you may create a thread dump file from here).

## Installation
SystemInfo requires Java 1.7 or above. 

Build the war file and then deploy the freshly build war file (SystemInfo.war) to your app server 
using the relevant deployment procedure. Once deployed SystemInfo can be accessed via a web browser 
at the context /SystemInfo.

## Authentication
SystemInfo used HTML basic authentication. You must create a user role 'systeminfo' and 
create or modify a user with this new role. Please refer to your application server documentation
for information on how to configure user role and create/modify user account. 

If you wish to disable HTML basic authentication please comment out the complete <security-constraint> 
section from the web.xml file.

## Using SystemInfo
Using a web browser point to the web context /SystemInfo as exponsed by your application server.
You'll be presented with a login form. After successful login you'll reach SystemInfo Home screen. Use 
the available menu options from the right hand sidebar to access various information pages.

## License
SystemInfo is released as a open source software under Apache License Version 2.0. Please
see browse to https://www.apache.org/licenses/LICENSE-2.0 for details of the provisions of this
license.


