
The goal here is to have a single reference version that can be propagated, so that when we generate a release the
version is reflected in all appropriate places

Unfortunately we need to manage different version format constraints:
* Maven versions are quite liberal in that almost any string can be a valid Maven version, but when comparing
versions strict rules are enforced (see https://octopus.com/blog/maven-versioning-explained).
* The web part will be versioned using NPM which enforces strict semantic versioning.
* Windows executables need to conform to a strict "4 numbers version", usually major.minor.revision.build

It should be noted here that semantic versioning only makes sense when talking about an API; what's a breaking
change when talking about an end-user application?

Proposal.
* The Maven version of the parent POM is the master version.
* It if formatted as YY.FI.NFI where
    - YY is the year (2 digits)
    - FI is an integer representing functional increment, starting at 1, incremented for each release that provides
     new functionality, reset each year
    - NFI is an integer representing a non-functional increment (i.e. a bugfix), starting at 0, reset with each FI
Examples: 
    19.1.0 is the first release of 2019
    19.1.1 is the first bugfix of this version
    19.2.0 provides additional functionality over 19.1.0   


  