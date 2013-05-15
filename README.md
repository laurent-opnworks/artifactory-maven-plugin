artifactory-maven-plugin
========================

This plugin can be used to delete snaphot versions of released artifacts. 

For example, if <b>foo-bar-1.0.0</b> was released/published to Artifactory 
then the plugin will delete the <b>foo-bar-1.0.0-SNAPSHOT</b> from the 
SNAPSHOTS repo.

### Usage

~~~~~~~~~~~~~~~~~~~
mvn com.opnworks.maven:artifactory-maven-plugin:[VERSION]:removeSnapshots \
	-Dartifactory.serviceUrl=http[s]://[HOST]:[PORT]/[ARTIFACTORY_CONTEXT_ROOT] \
	-Dartifactory.snapshotsRepoKey=SNAPSHOTS_REPO \
	-Dartifactory.releasesRepoKey=RELEASES_REPO \
	-Dartifactory.serverId=SERVER_ID \
	-Dartifactory.dryRun=[true | false] \
	-Dartifactory.rootPath=MY/ROOT/PATH
~~~~~~~~~~~~~~~~~~~
	
Where:
~~~~~~~~~~~~~~~~~~~
	SERVER_ID : ID of <server> element in Maven settings.xml containing
				artifactory user credentials 
~~~~~~~~~~~~~~~~~~~

### Example

~~~~~~~~~~~~~~~~~~~
mvn com.opnworks.maven:artifactory-maven-plugin:1.0.0:removeSnapshots \ 
	-Dartifactory.serviceUrl=http://repo.jfrog.org/artifactory \
	-Dartifactory.snapshotsRepoKey=libs-snapshots-local \
	-Dartifactory.releasesRepoKey=libs-releases-local \
	-Dartifactory.serverId=artifactory-jfrog \
	-Dartifactory.dryRun=true \
	-Dartifactory.rootPath=org/apache/maven
~~~~~~~~~~~~~~~~~~~

### Sample Run

~~~~~~~~~~~~~~~~~~~
...
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] Building Maven Stub Project (No POM) 1
[INFO] ------------------------------------------------------------------------
[INFO]
[INFO] --- artifactory-maven-plugin:0.0.4:removeSnapshots (default-cli) @ standalone-pom ---
2013-05-13_15:04:02.221 INFO  o.a.c.b.spring.BusApplicationContext - Loaded configuration file jar:file:/d:/repository/com/opnworks/maven/ar
tifactory-maven-plugin/0.0.4/artifactory-maven-plugin-0.0.4.jar!/client/client.xml.
2013-05-13 15:04:02 org.springframework.beans.factory.xml.XmlBeanDefinitionReader loadBeanDefinitions
INFO: Loading XML bean definitions from class path resource [META-INF/cxf/cxf.xml]
2013-05-13 15:04:02 org.springframework.beans.factory.xml.XmlBeanDefinitionReader loadBeanDefinitions
INFO: Loading XML bean definitions from URL [jar:file:/d:/repository/com/opnworks/maven/artifactory-maven-plugin/0.0.4/artifactory-maven-plu
gin-0.0.4.jar!/client/client.xml]
2013-05-13 15:04:02 org.springframework.beans.factory.support.DefaultListableBeanFactory preInstantiateSingletons
INFO: Pre-instantiating singletons in org.springframework.beans.factory.support.DefaultListableBeanFactory@12e5c94: defining beans [cxf,org.apache.cxf.bus.spring.BusWiringBeanFactoryPostProcessor,org.apache.cxf.bus.spring.Jsr250BeanPostProcessor,org.apache.cxf.bus.spring.BusExtensionPostProcessor,storage.proxyFactory,storage]; root of factory hierarchy
[INFO] About to delete: http://MY_SERVER/artifactory/repo-snapshots/com/opnworks/maven/demo-app-foo/1.0.10-SNAPSHOT
[INFO] Deleted: com/opnworks/maven/demo-app-foo/1.0.10-SNAPSHOT = 204
[INFO] About to delete: http://MY_SERVER/artifactory/repo-snapshots/com/opnworks/maven/demo-app-bar/1.0.10-SNAPSHOT
[INFO] Deleted: com/opnworks/maven/demo-app-bar/1.0.10-SNAPSHOT = 204
2013-05-13 15:04:17 org.springframework.context.support.AbstractApplicationContext doCloseINFO: Closing org.apache.cxf.bus.spring.BusApplicationContext@1eb717e: startup date [Mon May 13 15:04:01 EDT 2013]; root of context hierarchy
2013-05-13 15:04:17 org.springframework.beans.factory.support.DefaultSingletonBeanRegistry destroySingletons
INFO: Destroying singletons in org.springframework.beans.factory.support.DefaultListableBeanFactory@12e5c94: defining beans [cxf,org.apache.cxf.bus.spring.BusWiringBeanFactoryPostProcessor,org.apache.cxf.bus.spring.Jsr250BeanPostProcessor,org.apache.cxf.bus.spring.BusExtensionPostProcessor,storage.proxyFactory,storage]; root of factory hierarchy
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 20.141s
[INFO] Finished at: Mon May 13 15:04:17 EDT 2013
[INFO] Final Memory: 4M/15M
[INFO] ------------------------------------------------------------------------
~~~~~~~~~~~~~~~~~~~