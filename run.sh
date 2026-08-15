#!/bin/sh
set -eu
rm -rf out
mkdir -p out
javac --add-modules jdk.httpserver -d out $(find src -name "*.java")
java --add-modules jdk.httpserver -cp out com.university.research.Main
