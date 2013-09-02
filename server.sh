#!/bin/bash

exec java -jar jetty-runner-8.1.11.v20130520.jar --port 8191 --path /dp dw-data-profiling-0.0.1-SNAPSHOT.war >server.log 2>&1

