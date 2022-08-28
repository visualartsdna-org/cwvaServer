
rem https://stackoverflow.com/questions/4955635/how-to-add-local-jar-files-to-a-maven-project
mvn install:install-file -Dfile=target\cwvaServer-2.0.4.jar -DgroupId=org.visualartsdna -DartifactId=cwvaServer -Dversion=2.0.4 -Dpackaging=jar -DgeneratePom=true
