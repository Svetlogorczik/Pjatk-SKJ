@echo off
javac Proxy.java
javac TCPServer.java
javac UDPServer.java
javac TCPClient.java
javac UDPClient.java
ping 127.0.0.1 -n 2
start cmd /k java Proxy -port 60001 -server 127.0.0.1 50001 -server 127.0.0.1 60002 -server 127.0.0.1 60003 -server 127.0.0.1 60004
start cmd /k java Proxy -port 60002 -server 127.0.0.1 50002 -server 127.0.0.1 60001 -server 127.0.0.1 60003
start cmd /k java Proxy -port 60003 -server 127.0.0.1 50001 -server 127.0.0.1 60002 -server 127.0.0.1 60004
start cmd /k java Proxy -port 60004 -server 127.0.0.1 50002 -server 127.0.0.1 60001 -server 127.0.0.1 50003
ping 127.0.0.1 -n 2
start cmd /k java TCPServer -port 50001 -key a -value 1
start cmd /k java TCPServer -port 50002 -key b -value 2
ping 127.0.0.1 -n 2
start cmd /k java UDPServer -port 50003 -key c -value 3
ping 127.0.0.1 -n 2
:: wait for 1 second to update all proxies before sending clients
start cmd /k java UDPClient -address 127.0.0.1 -port 60001 -command GET NAMES
start cmd /k java TCPClient -address 127.0.0.1 -port 60001 -command GET VALUE a
start cmd /k java TCPClient -address 127.0.0.1 -port 60001 -command SET a 5
ping 127.0.0.1 -n 2
start cmd /k java TCPClient -address 127.0.0.1 -port 60001 -command GET VALUE a
