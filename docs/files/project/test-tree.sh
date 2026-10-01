#!/bin/sh
# Test network: a TREE of proxies (no cycles), TCP and UDP key servers mixed.
#
#            client
#              |
#           [P1:9001] --- TCP a=10
#            /      \
#   [P2:9002]        [P3:9003] --- UDP c=30
#       |                 |
#   UDP b=20          TCP d=40
#
# Put this script next to: TCPServer, UDPServer, TCPClient, UDPClient (from the course) and YOUR Proxy.
javac *.java || exit 1

java TCPServer -port 8101 -key a -value 10 &
java UDPServer -port 8102 -key b -value 20 &
java UDPServer -port 8103 -key c -value 30 &
java TCPServer -port 8104 -key d -value 40 &
sleep 1

java Proxy -port 9002 -server localhost 8102 &
java Proxy -port 9003 -server localhost 8103 -server localhost 8104 &
sleep 1
java Proxy -port 9001 -server localhost 8101 -server localhost 9002 -server localhost 9003 &
sleep 2

echo "=== expected: OK 4 a b c d (any order)"
java TCPClient -address localhost -port 9001 -command GET NAMES
java UDPClient -address localhost -port 9001 -command GET NAMES
echo "=== expected: OK 40 (TCP client -> ... -> TCP server behind P3)"
java UDPClient -address localhost -port 9001 -command GET VALUE d
echo "=== expected: OK, then OK 99"
java TCPClient -address localhost -port 9002 -command SET c 99
java UDPClient -address localhost -port 9003 -command GET VALUE c
echo "=== expected: NA (no such key)"
java TCPClient -address localhost -port 9001 -command GET VALUE zzz
echo "=== QUIT: everything should stop"
java TCPClient -address localhost -port 9001 -command QUIT
sleep 2
jobs
