rem without -SNAPSHOT suffix in the version and autoReleaseAfterClose=false in the nexus-staging-maven-plugin
set MAVEN_BATCH_PAUSE=on
mvn clean source:jar javadoc:jar deploy