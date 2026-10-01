#!/bin/sh
# Test network WITH A CYCLE (needed for 500 points): P1 -> P2 -> P3 -> P1, plus a shortcut P1 -> P3.
#
#   [P1:9101] ---> [P2:9102] ---> [P3:9103]
#       ^  \__________________________/ |
#       |_______________________________|
#   key servers: a (TCP) at P1, b (UDP) at P2, c (UDP) at P3
#
# A correct proxy must NOT loop forever and must answer every command exactly once.
javac *.java || exit 1

java TCPServer -port 8201 -key a -value 1 &
java UDPServer -port 8202 -key b -value 2 &
java UDPServer -port 8203 -key c -value 3 &
sleep 1

java Proxy -port 9101 -server localhost 8201 -server localhost 9102 -server localhost 9103 &
java Proxy -port 9102 -server localhost 8202 -server localhost 9103 &
java Proxy -port 9103 -server localhost 8203 -server localhost 9101 &
sleep 3

echo "=== expected: OK 3 a b c   (each name ONCE, counter = 3)"
java TCPClient -address localhost -port 9102 -command GET NAMES
echo "=== expected: OK 1"
java UDPClient -address localhost -port 9103 -command GET VALUE a
echo "=== expected: OK, then OK 7"
java TCPClient -address localhost -port 9101 -command SET b 7
java TCPClient -address localhost -port 9103 -command GET VALUE b
echo "=== QUIT"
java UDPClient -address localhost -port 9102 -command QUIT
sleep 2
jobs
