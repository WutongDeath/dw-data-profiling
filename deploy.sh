#!/bin/bash

mvn clean package
scp target/dw-data-profiling-0.0.1-SNAPSHOT.war dw-95:/data2/data-profiling/

mvn clean package -Dmy.packaging=jar
scp target/dw-data-profiling-0.0.1-SNAPSHOT-jar-with-dependencies.jar dw-95:/data2/data-profiling/

