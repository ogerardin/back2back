
#backup process

The user defines a set of backup sources (currently only filesystem directories are supported) and backup targets
(currently only local and peer back2back are supported).

A backup set represents the state of backup from a given source to a given target. Currently a backup set is created for
each combination of source+target, i.e. each source is backed up to each target (this might change). 
For each backup set we have a local "hash database" which stores the hash of the latest backed up revision of each 
source file. 

Each backup set is powered by a backup job.

##backup job scenario

###step 0
- the flag "deleted" is set to true for all records in the hash database. This flag will be set to false for each actual 
file that we examine, so that in the end the only remaining entries with flag "deleted" set will be files that
have actually been deleted since the last backup

###step 1
- we list each file from the backup source
- for each file, we compute a hash and compare it to the previous hash (if there was one) to determine if the file has
changed since last backup and hence must be backed up.
- if the file must be backed up, we set the "backup requested" flag to true in the hash database

###step 2
- we read each entry from the hash database that has either "deleted" or "backup requested" flags set to true
- if the file has "deleted" flag, we mark it as deleted in the backup target
- if the file has "backup requested" flag, we store it to the backup target

###step 3
- entries in the hash database with flag "deleted" are removed
    

