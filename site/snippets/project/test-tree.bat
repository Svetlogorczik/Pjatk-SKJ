@echo off
rem Windows version of test-tree.sh - every process in its own window.
javac *.java
start "a TCP" cmd /k java TCPServer -port 8101 -key a -value 10
start "b UDP" cmd /k java UDPServer -port 8102 -key b -value 20
start "c UDP" cmd /k java UDPServer -port 8103 -key c -value 30
start "d TCP" cmd /k java TCPServer -port 8104 -key d -value 40
timeout /t 1 > nul
start "P2" cmd /k java Proxy -port 9002 -server localhost 8102
start "P3" cmd /k java Proxy -port 9003 -server localhost 8103 -server localhost 8104
timeout /t 1 > nul
start "P1" cmd /k java Proxy -port 9001 -server localhost 8101 -server localhost 9002 -server localhost 9003
timeout /t 2 > nul
java TCPClient -address localhost -port 9001 -command GET NAMES
java UDPClient -address localhost -port 9001 -command GET VALUE d
java TCPClient -address localhost -port 9002 -command SET c 99
java UDPClient -address localhost -port 9003 -command GET VALUE c
java TCPClient -address localhost -port 9001 -command GET VALUE zzz
pause
java TCPClient -address localhost -port 9001 -command QUIT
