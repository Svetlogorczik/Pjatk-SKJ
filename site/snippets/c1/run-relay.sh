#!/bin/sh
# Compile once, start B and C in the background, then A in the foreground.
javac RelayNode.java || exit 1
java RelayNode relay-b.properties &
java RelayNode relay-c.properties &
sleep 1
java RelayNode relay-a.properties
wait
