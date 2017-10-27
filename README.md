back2back
=========

The ultimate goal of this project is to provide a replacement for the
"backup to a friend" feature of Code42's CrashPlan (and also local
backup).

Architecture
------------
- Spring Boot application with Spring MVC
- Embedded MongoDB for storing configuration and files (via GridFS)
- backup jobs managed using Spring Batch
- web interface using Vue.js

Status
------
Very preliminary stage, can backup files from a directory to GridFS,
no usable GUI. 