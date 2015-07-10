java -Xmx1g -XX:+TieredCompilation -XX:ReservedCodeCacheSize=256m -XX:MaxPermSize=512m -XX:+UseNUMA -XX:+UseParallelGC -XX:+CMSClassUnloadingEnabled -jar ../lib/sbt-launch.jar %*
