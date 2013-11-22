# Simple Persistence API for Salesforce

A CRUD-based interface for interacting with records in Salesforce through the use of annotated Javabeans.

## Building from Source

### Prequisities
[Gradle](http://www.gradle.org/) is used as the build tool and [git](http://git-scm.com/) for source control. Here's what you'll need to do the first time you try to build:
* Download and install java  (version 1.7.0_40 or better)
* Download and install git

You don't need to preinstall gradle because it will dynamically download

### Build Steps
1. Check out the source with the following command:

`git clone git@github.com:davidbuccola/force-spa.git`

2. Change your current working directory to the source you just retrieved:

`cd force-spa`

3. Build (includes running the unit tests)

`./gradlew build`
