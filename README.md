[![Build Status](https://travis-ci.org/ogerardin/back2back.svg?branch=master)](https://travis-ci.org/ogerardin/back2back)

back2back
=========

The inspiration for this project is to provide a replacement for the "backup to a friend" feature of Code42's CrashPlan,
which is going away in october 2018: https://www.code42.com/news-releases/code42-focus-business/


Goals and assumptions
-----
- Local backup (from filesystem)
- Peer-to-peer backup (and restore) through standard web protocols
- No need for a centralized account (unlike CrashPlan). 
- Remote computer should be reachable via a URL, which implies the use of a fixed IP address or dynamic DNS. 
- Remote computer should also accept incoming connections, which might impliy some firewall/router configuration, 
but this is a fairly common issue.  
- Security: authentication of remote computer, encryption of backups 
- Cross-platform with idiomatic installation (e.g. setup.exe on Windows) and good native integration (e.g. as a Windows 
service on Windows)


Architecture
------------
- Spring Boot application with Spring MVC
- Embedded MongoDB for storing configuration and files (via GridFS)
- Backup jobs implemented as Spring Batch jobs
- Web interface using Vue.js


Status
------
Very preliminary stage: can backup files from a directory to GridFS and to the local peer through HTTP. Basic and 
incomplete GUI. 

Installing back2back
====================
Back2back is composed of 2 parts:
* a daemon that runs in the background, performs the backup operations and provides a web GUI.
* a system tray icon app that allows quick access to the GUI and provides control over the daemon.

Note: Automatic installation through an installer or a package management system is not yet available.

Manual Installation
-------------------
Download the archive (zip or tgz).

Unzip the archive into a directory of your choice. This will be Back2back's home directory.
* Under Windows, it is recommended to use a directory at the root of the disk, such as C:\back2back
* Under macOS, a common choice is /Applications/back2back

Starting the daemon
-------------------
The daemon consists of a standalone executable jar named back2back-bundle-repackaged.jar. If Java is is correctly 
configured on your system, you can start the daemon by double-clicking on the jar file's icon in the file explorer, but 
it is not the recommended way because it doesn't have a proper GUI and you will not get feedback.

To start it the first time and check that it starts correctly, open a command prompt, cd into the directory where the
jar file resides, and type:

    java -jar @back2back.core.jar@

The daemon will start up and begin displaying log messages to the screen.
During the first start, you need an internet connection because the daemon will download a copy of MongoDB, the database
it uses to store configuration and backup files.
When the log displays the message "Started Main in x.xxxx seconds", the daemon is ready and you can use the GUI.

Accessing the GUI
-----------------
Back2back's GUI is a web application; when the daemon is ready you can access it by opening a web browser and typing
the address: http://localhost:8080
A web page with Back2back's logo should open.

Stopping the daemon
-------------------
To stop the daemon the cleanest way is to issue a shutdown command via the HTTP API. You can do this using
the web interface (Admin/Shutdown), or from the command line using curl:

    curl http://localhost:8080/api/app/shutdown

Configuring the daemon for automatic start
------------------------------------------
When you have confirmed that the daemon runs correctly, you should configure your system to start it automatically at
boot. There are a number of ways to do this, depending on the operating system:
* on Windows, the recommended way is to configure it as a Windows Service
* on macOS, the recommended way is to configure it as a launchd Global Daemon

Configuring as a Windows service
--------------------------------
The archive comes with a copy of [NSSM](http://nssm.cc).
To install back2back daemon as a Windows service, just run:

    installService.bat

The service is installed with name "back2back"

To uninstall type:

    removeService.bat


Configuring as a macOS Global Daemon
------------------------------------
To install back2back daemon as a macOS "global daemon" using launchtl, just run:

    sudo ./installDaemon.sh

You will need to enter your password. This will only work if your user has administrative rights.

To uninstall type:

    sudo ./removeDaemon.sh








