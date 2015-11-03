#!/usr/bin/env bash
mvn clean package
boxfuse fuse target/whiskyreturns-0.3.jar -jvm.args=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -ports.tracing=5005
boxfuse run whiskyreturns:0.3 -portsmap.tracing=5105 -envvars.CONSUL_SKIP_DEPLOYMENT=true -envvars.CONSUL_AGENT_ADDRESS=192.168.0.15 -envvars.WITHOUT_STUBS=true