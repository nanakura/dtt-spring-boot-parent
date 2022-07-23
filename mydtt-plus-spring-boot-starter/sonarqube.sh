#!/bin/bash

SETTINGS="/Users/weasley/Development/program/apache-maven/conf/settings-sonarqube.xml"
MODULE="mydtt-plus-spring-boot-starter"
clear &&
  mvn clean compile sonar:sonar -pl :$MODULE -am --settings $SETTINGS &&
  mvn clean
