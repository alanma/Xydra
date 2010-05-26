/**
 * TODO remove dependency to Wadl plugin from this project
 * Explanation: including com.sun.jersey.server.wadl.WadlGenerator requires a maven
 * dependency to com.sun.jersey.contribs:maven-wadl-plugin leads to having
 * eclipse dependencies to all these jars:
 * 
 * <pre>
 * [INFO] |  +- com.sun.jersey.contribs:maven-wadl-plugin:jar:1.1.5:compile
 * [INFO] |  |  +- org.apache.maven:apache-maven:jar:2.0.9:compile
 * [INFO] |  |  |  +- org.apache.maven:maven-core:jar:2.0.9:compile
 * [INFO] |  |  |  |  +- org.apache.maven:maven-settings:jar:2.0.9:compile
 * [INFO] |  |  |  |  +- org.apache.maven.wagon:wagon-file:jar:1.0-beta-2:runtime
 * [INFO] |  |  |  |  +- org.apache.maven:maven-plugin-parameter-documenter:jar:2.0.9:compile
 * [INFO] |  |  |  |  +- org.apache.maven.wagon:wagon-webdav:jar:1.0-beta-2:runtime
 * [INFO] |  |  |  |  |  \- slide:slide-webdavlib:jar:2.1:runtime
 * [INFO] |  |  |  |  |     +- commons-httpclient:commons-httpclient:jar:2.0.2:runtime
 * [INFO] |  |  |  |  |     +- jdom:jdom:jar:1.0:runtime
 * [INFO] |  |  |  |  |     \- de.zeigermann.xml:xml-im-exporter:jar:1.1:runtime
 * [INFO] |  |  |  |  +- org.apache.maven.wagon:wagon-http-lightweight:jar:1.0-beta-2:runtime
 * [INFO] |  |  |  |  |  +- org.apache.maven.wagon:wagon-http-shared:jar:1.0-beta-2:runtime
 * [INFO] |  |  |  |  |  |  \- jtidy:jtidy:jar:4aug2000r7-dev:runtime
 * [INFO] |  |  |  |  |  \- xml-apis:xml-apis:jar:1.0.b2:runtime
 * [INFO] |  |  |  |  +- org.apache.maven.reporting:maven-reporting-api:jar:2.0.9:compile
 * [INFO] |  |  |  |  |  \- org.apache.maven.doxia:doxia-sink-api:jar:1.0-alpha-10:compile
 * [INFO] |  |  |  |  +- org.apache.maven:maven-profile:jar:2.0.9:compile
 * [INFO] |  |  |  |  +- org.apache.maven:maven-model:jar:2.0.9:compile
 * [INFO] |  |  |  |  +- org.apache.maven.wagon:wagon-provider-api:jar:1.0-beta-2:compile
 * [INFO] |  |  |  |  +- org.codehaus.plexus:plexus-container-default:jar:1.0-alpha-9-stable-
 * [INFO] |  |  |  |  +- org.apache.maven:maven-repository-metadata:jar:2.0.9:compile
 * [INFO] |  |  |  |  +- org.apache.maven:maven-error-diagnostics:jar:2.0.9:compile
 * [INFO] |  |  |  |  +- org.apache.maven:maven-project:jar:2.0.9:compile
 * [INFO] |  |  |  |  |  \- org.apache.maven:maven-plugin-registry:jar:2.0.9:compile
 * [INFO] |  |  |  |  +- commons-cli:commons-cli:jar:1.0:compile
 * [INFO] |  |  |  |  +- org.apache.maven.wagon:wagon-ssh-external:jar:1.0-beta-2:runtime
 * [INFO] |  |  |  |  |  \- org.apache.maven.wagon:wagon-ssh-common:jar:1.0-beta-2:runtime
 * [INFO] |  |  |  |  +- org.apache.maven:maven-plugin-descriptor:jar:2.0.9:compile
 * [INFO] |  |  |  |  +- org.codehaus.plexus:plexus-interactivity-api:jar:1.0-alpha-4:compile
 * [INFO] |  |  |  |  +- org.apache.maven:maven-artifact-manager:jar:2.0.9:compile
 * [INFO] |  |  |  |  +- org.apache.maven:maven-monitor:jar:2.0.9:compile
 * [INFO] |  |  |  |  +- org.apache.maven.wagon:wagon-ssh:jar:1.0-beta-2:runtime
 * [INFO] |  |  |  |  |  \- com.jcraft:jsch:jar:0.1.27:runtime
 * [INFO] |  |  |  |  \- classworlds:classworlds:jar:1.1:compile
 * [INFO] |  |  |  \- org.apache.maven:maven-toolchain:jar:2.0.9:compile
 * [INFO] |  |  +- org.apache.maven:maven-artifact:jar:2.0.9:compile
 * [INFO] |  |  |  \- org.codehaus.plexus:plexus-utils:jar:1.5.1:compile
 * [INFO] |  |  +- org.apache.maven:maven-plugin-api:jar:2.0:compile
 * [INFO] |  |  \- xerces:xercesImpl:jar:2.6.1:compile
 * [INFO] |  +- com.sun.jersey.contribs:wadl-resourcedoc-doclet:jar:1.1.5:compile
 * [INFO] |  +- javax.xml.bind:jaxb-api:jar:2.1:compile
 * [INFO] |  |  +- javax.xml.stream:stax-api:jar:1.0-2:compile
 * [INFO] |  |  \- javax.activation:activation:jar:1.1:compile
 * </pre>
 * which in turn leads to all of them being included in AppEngine apps using CXM.
 */
package org.xydra.server.wadl;

