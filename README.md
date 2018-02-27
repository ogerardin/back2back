back2back
=========

The inspiration for this project is to provide a replacement for the "backup to a friend" feature of Code42's CrashPlan,
which is going away in october 2018.


Goals and assumptions
-----
- Cross-platform
- ...


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