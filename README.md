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