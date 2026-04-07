#!/usr/bin/env bash
set -e

# installa Java + Maven
apt-get update
apt-get install -y openjdk-17-jdk maven

# build progetto
mvn clean package
