#!/usr/bin/env bash
./bin/consul agent -data-dir ./tmp -config-dir ./config -server -bootstrap-expect 1 -ui-dir ./dist