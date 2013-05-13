artifactory-maven-plugin
========================

Maven plugin for Artifactory

*** Usage

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

*** Example

~~~~~~~~~~~~~~~~~~~
mvn com.opnworks.maven:artifactory-maven-plugin:1.0.0:removeSnapshots \ 
	-Dartifactory.serviceUrl=http://repo.jfrog.org/artifactory \
	-Dartifactory.snapshotsRepoKey=libs-snapshots-local \
	-Dartifactory.releasesRepoKey=libs-releases-local \
	-Dartifactory.serverId=artifactory-jfrog \
	-Dartifactory.dryRun=true \
	-Dartifactory.rootPath=org/apache/maven
~~~~~~~~~~~~~~~~~~~

*** Sample response with dryRun=true

