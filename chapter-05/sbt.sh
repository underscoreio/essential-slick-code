#!/usr/bin/env bash

java -XX:+TieredCompilation -XX:ReservedCodeCacheSize=256m -XX:+UseNUMA -XX:+UseParallelGC -XX:+CMSClassUnloadingEnabled -jar ../lib/sbt-launch.jar "$@"
