# Kmeel
Simple cross-platform forensic program for processing email files. <br/>
Can be used to quickly find that one email to use as evidence in court.

## Installation
Kmeel requires [Java](http://www.oracle.com/technetwork/java/javase/downloads/index.html) 8 or later to be installed.

### From source:
Compiling from source requires [Git](https://git-scm.com/) and [Maven](https://maven.apache.org/) to be installed.
```
git clone https://github.com/Marten4n6/Kmeel.git
cd Kmeel/
mvn package clean install
java -jar Kmeel.jar
```

## Plugins
Kmeel uses [pf4j](https://github.com/decebals/pf4j) which allows you to easily create plugins. <br/>
Once you've read how pf4j works simply implement any interface from [here](https://github.com/Marten4n6/Kmeel/tree/master/KmeelAPI/src/main/java/com/github/kmeel/api/spi). <br/>
Examples of plugins can be found [here](https://github.com/Marten4n6/Kmeel/tree/master/plugins).

## Versioning
Kmeel will be maintained under the Semantic Versioning guidelines as much as possible. <br/>
Releases will be numbered with the follow format: <br/>
`<major>.<minor>.<patch>`

And constructed with the following guidelines:
* Breaking backward compatibility bumps the major
* New additions without breaking backward compatibility bumps the minor
* Bug fixes and misc changes bump the patch

For more information on SemVer, please visit http://semver.org/.
