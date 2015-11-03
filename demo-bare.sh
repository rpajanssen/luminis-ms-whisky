#!/usr/bin/env bash
mvn clean package
java -jar target/whiskyreturns-0.3.jar -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -DCONSUL_SKIP_DEPLOYMENT=true -DCONSUL_AGENT_ADDRESS=192.168.0.15 -DBOXFUSE_PORTS_FORWARDED_HTTP=80 -DWITHOUT_STUBS=true -DBOXFUSE_ENV=test
