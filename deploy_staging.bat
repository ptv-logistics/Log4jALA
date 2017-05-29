rem without -SNAPSHOT suffix in the version and autoReleaseAfterClose=false in the nexus-staging-maven-plugin
mvn clean source:jar javadoc:jar deploy