
                                        Installing back2back


Back2back is composed of 2 parts:
* a daemon that runs in the background, performs the backup operations and provides a web GUI.
* a system tray icon app that allows quick access to the GUI and provides control over the daemon.

Manual installation
===================
Unzip the archive into a directory of your choice. This will be Back2back's home directory.
* Under Windows, it is recommended to use a directory at the root of the disk, such as C:\back2back
* Under macOS, a common choice is /Applications/back2back

Starting the daemon
-------------------
The daemon consists in a standalone executable jar named @back2back.core.jar@. Although if your system is correctly
configured, you can start it by double-clicking on the jar file's icon in the file explorer, it is not the recommended
way to start it because it doesn't have a proper GUI and you will not get feedback.

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

Configuring the daemon for automatic start
------------------------------------------
When you have confirmed that the daemon runs correctly, you should configure your system to start it automatically at
boot. There are a number of ways to do this, depending on the operating system:
* on Windows, the recommended way is to configure it as a Windows Service
* on macOS, the recommended way is to configure it as a launchd Global Daemon

Configuring as a Windows service
--------------------------------
The archive comes with a copy of NSSM.
//TODO

Configuring as a maxOS Global Daemon
------------------------------------
//TODO







