#!/usr/bin/env bash

java -Xmx2g -Xms2g -XX:+TieredCompilation -XX:ReservedCodeCacheSize=256m -XX:MaxPermSize=512m -XX:+UseNUMA -XX:+UseParallelGC -XX:+CMSClassUnloadingEnabled -jar ../lib/sbt-launch.jar "$@"
