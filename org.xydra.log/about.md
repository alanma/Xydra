## Logging

# target platforms:

## GWT
Configured in .gwt.xml module metadata file

## Pure Java (Desktop)
Config via log4j.properties

## AppEngine
Config in /src/main/webapp/WEB-INF/java-util-logging.properties and enabled in appengine-web.xml

# projects

org.xydra.log
: Basic logging API

org.xydra.log.ext
: UniversalLogger, using xydra.conf, can run in GWT, GAE and Pure Java

org.xydra.log.howto 
: slightly outdated

# Runtime config
Use LogConfTool and keys 'log.(classname)'