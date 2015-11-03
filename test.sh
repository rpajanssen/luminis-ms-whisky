#!/usr/bin/env bash
mvn clean package
boxfuse rm whiskyreturns
boxfuse fuse target/whiskyreturns-0.3.jar -jvm.args=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -ports.tracing=5005 -ports.consul-http=8500 -ports.consul-dns=8600 -ports.consul-cli-rpc=8400 -ports.consul-rpc=8300 -ports.consul-lan=8301 -ports.consul-wan=8302 -ports.consul-lan=8301/udp -ports.consul-wan=8302/udp
boxfuse run whiskyreturns:0.3 -portsmap.tracing=5105 -portsmap.consul-http=8500 -portsmap.consul-dns=8600 -portsmap.consul-cli-rpc=8400 -portsmap.consul-rpc=8300 -portsmap.consul-lan=8301 -portsmap.consul-wan=8302 -envvars.RUN_WITH_CONSUL_SERVER=true
