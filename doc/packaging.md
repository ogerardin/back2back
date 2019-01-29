
One of the goals of the project is to provide an idiomatic installation process for Windows, macOS and Linux. Furthermore, 
ideally (although it's not a primary goal) all the installers/packages should be built by the Maven module "dist" 
independantly of the platform it runs on, meaning the packages for all platforms should be generated whether Maven runs 
on Windows, Mac or Linux. This might prove difficult for the Mac package, which requires tools that only run on macOS.

Packaging considerations
========================

JRE
---
back2back requires Java 1.8 or later to run. Some tools will allow bundling a JRE together with the application to be 
installed; we consider that this prerequisite is already met by most of the target environments, and so we will not
include a JRE in the distribution packages. That said, the installation packages should at least check that a compatible 
version of the JRE is available, and if not offer to go to the download site or even better download and install it 
automatically.

MongoDB
-------
The appropriate MongoDB version is normally downloaded automatically during the first launch of the back2back engine. 
However, this process delays the startup of the engine by a considerable time, and unless it's started manually from a 
shell, there is currently no feedback during this time.
To eliminate this problem, we have the following options:
* include MongoDB in the distribution package: this adds more than 200 Mb to the size of the distributable, and we 
would need to make one version for each target platform. 
* (preferred) the installer should include the possibility to download MongoDB during the installation
process, with full user feedback.    

service manager
---------------
As part of a native installation, the back2back engine should be installable as a native service
* for Windows, it means a native Windows service. NSSM works fine with java code, is lightweight and has a compatible 
license, so we will bundle it and use it. NSSM is downloaded by Maven using the Maven download plugin during the 
"generate-resources" phase, so it's available to all packagers. 
* for macOS, we will use the native launchd system. No additional tool is required as they are part of macOS.
* for Linux: TBD  

Distribution formats and tools
==============================

zip/tar.gz
----------
Basic packaging, with some OS-specific scripts to install/remove the service, launch the engine or the tray icon.
Requires some knowledge of the command line to install.


IzPack
------
IzPack (http://izpack.org/) generates a cross-platform Java installer that will work on all target platforms, provided
there is a JRE installed. However it has some inconveniences:
* the look and feel is not native  
* it doesn't provide a native executable (although this could be worked around using a native wrapper)
* it doesn't integrate with the OS in an idiomatic way
* it doesn't handle well the steps that require specific priviledges
* it's only extensible through additional panels
As such, it's not a satisfactory solution and should be considered only as a fallback

launch4j
--------
Launch4J (http://launch4j.sourceforge.net/) is a wrapper that is able to generate a native application (for Windows, macOS
or Linux) that embeds the application jar, its dependencies and optionally a JRE, 
Launch4j may be used to wrap the IzPack installer into a native executable for each platform.


Inno Setup
----------
Inno Setup (http://www.jrsoftware.org/isinfo.php) is a very complete and reliable solution to generate Windows native 
installers. Unfortunately it's only available for Windows, but thanks to Wine we are able to run it on other platforms
with slight changes in the way we invoke it.

Through some custom scripting, the Inno Setup installer also does the following:
* check that we have a version of the JRE that meets the requirements, and if not offer to go to the Oracle site and 
download one
* offer to pre-download a copy of MongoDB, so that it doesn not have to be doanload at forst run of the engine.

How we generate the installer:
* Windows: to minimize the number of dependencies required, we install Inno Setup and the Inno Download Plugin using
scoop (https://scoop.sh/). These apps are not available in the default bucket, so we provide our own manifests 
(dist/src/main/innosetup/*.json)
* Wine: 
    1) we download and extract innounp, a utility to unpack Inno Setup installers. It's a Windows executable so we will 
    need WIne to run it
    2) we download the installer for Inno Setup and unpack it using Wine + innounp
    3) we download the installer for Inno Download Plugin and unpack it using Wine +innounp
    4) we run the Inno Compiler using Wine
    
Mac
---
Traditionally the Mac platform has had two ways to install an app:
* by copying an app bundle to the Applications folder
* by running a package installer (wizard) generated using Apple tools pkgbuild and productbuilder
The first method is more intuitive, but the app might need to take care of some initial configuration steps during its
first launch. The second one is more familiar to Windows users, and may be more suitable if there re actions to execure 
before and/or after the installation.

TO BE DONE

Linux
-----
We should at least provide a .deb package

TO BE DONE

  
 