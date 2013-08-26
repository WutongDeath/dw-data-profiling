DW Data Profiling
=================

Run Web Application
-------------------

```
$ mvn jetty:run
```

Run Builder Job
---------------

```
$ mvn compile exec:java -Dexec.mainClass=com.anjuke.dw.data_profiling.job.Builder
```

Packaging
---------

### WAR

Download Jetty runner Jar from [Maven](http://search.maven.org/#search%7Cga%7C1%7Cjetty-runner).

```
$ mvn clean package
$ java -jar jetty-runner.jar --port 8191 --path /dp target/dw-data-profiling-0.0.1-SNAPSHOT.war
```

### JAR

```
$ mvn clean package -Dmy.packaging=jar
$ java -jar target/dw-data-profiling-0.0.1-SNAPSHOT-jar-with-dependencies.jar
```
