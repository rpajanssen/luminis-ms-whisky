#!/usr/bin/env bash
mvn clean package
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -DCONSUL_SKIP_DEPLOYMENT=true -DBOXFUSE_PORTS_HTTP=8080 -DBOXFUSE_PORTS_FORWARDED_HTTP=8080 -DWITHOUT_STUBS=true -DBOXFUSE_ENV=test -jar target/whiskyreturns-0.3.jar server target/classes/demo.yml
