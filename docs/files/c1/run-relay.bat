@echo off
rem Compile once, then open every node in its own window.
javac RelayNode.java
start "Node B" cmd /k java RelayNode relay-b.properties
start "Node C" cmd /k java RelayNode relay-c.properties
timeout /t 1 > nul
start "Node A" cmd /k java RelayNode relay-a.properties
