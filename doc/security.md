
#back2back Security
(just a few ideas)


There are two kinds of security considerations that we should address:
- backup encryption, so that the stored files cannot be read by anyone
- API security, so that only authorized persons or computers can administer back2back or perform inbound backup

## Backup encryption
Currently GridFsStorageService supports storing a file with encryption (and retrieving it) using an instance of 
java.security.Key. 
This works but requires a mechanism for generating and storing the key to be used.
It should be noted that using this system, the filenames themselves are to encrypted.


##API security
The API provides 2 kinds of services:
- backend operations for the user interface (administration and configuration, file restoration, ...)
- incoming remote peer backup

Consequently, API users can be either:
- a human user through a frontend
- a remote computer (on behalf of a remote user) running an instance of back2back

Remote users performing a backup to the local computer should be able to remotely use the API to monitor backup 
progress and retrieve files. Their access must be limited to the backups originating from the associated computer.

Human users will be identified using a classic user/password combination (probably stored in MongoDB).
Remote computers will be identified by a unique UUID ("machine ID") randomly generated during the first start of the
back2back engine. 

The API should enforce that all remote accesses (where origin is not localhost) use HTTPS.

###Remote backup initiation protocol

Assumptions:
- user A is running an instance of back2back
- user B is also running an instance of back2back and wants to add A's computer as a backup destination.
- user B knows the public URL of A's back2back API

Sequence:
1. A generates a one-time code with a limited validity in time and communicates it to B using any communication channel
2. B adds a remote destination with  
    * the URL to access A's back2back instance 
    * the one-time code obtained from A
3. During the first connection to A, B calls a registration API using the one-time code and is assigned a password
4. For subsequent operations, B uses its machine ID and the assigned password to authenticate
 
  
 
 
 

  