# Simple Persistence API for Salesforce

## Introduction

The Simple Persistence API for Salesforce (SPA) is a high-level library that simplifies Java interactions with
Salesforce objects. SPA uses [Salesforce REST](http://www.salesforce.com/us/developer/docs/api_rest/) to communicate
with the server and is based on the popular [Jackson](http://wiki.fasterxml.com/JacksonHome) library for
JSON processing, but leverages Java annotations and built-in semantic knowledge about Salesforce objects and
relationships to enable powerful interactions that go beyond what can be done with basic Jackson alone.

## Presentations

* [Dreamforce 2013 - Simplifying Salesforce REST in Java using Annotations](./docs/DF13 - Simplifying Salesforce REST in Java using Annotations.pdf)

## Building from Source

### Prequisities for Building

SPA is built using [Gradle](http://www.gradle.org/), and as you aleady know, uses [Git](http://git-scm.com/) for
source control. Before building SPA from source you'll need to:

* Download and install git
* Download and install java (version 1.7.0_40 or better)

You don't need to install gradle because it will be dynamically download

### Build Steps

Check out the source with the following command:

```
git clone git@github.com:davidbuccola/force-spa.git
```

Change your current working directory to the source you just retrieved:

```
cd force-spa
```

Build (includes running the unit tests)

```
./gradlew build
```
