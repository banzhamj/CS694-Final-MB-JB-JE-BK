#!/bin/bash

IWARS_USER=elchisjm
IWARS_MON=helios.ececs.uc.edu
IWARS_MON_PORT=8160
IWARS_SERVER_PORT=20000

# avoid zombies.  ensure everything is stopped.
./stop.sh

# tunnel for active client
xterm -e ssh -L $IWARS_MON_PORT:$IWARS_MON:$IWARS_MON_PORT $IWARS_USER@$IWARS_MON &
echo "$!" > /tmp/iwars.ac.pid

# tunnel for passive server
xterm -e ssh -R $IWARS_SERVER_PORT:localhost:$IWARS_SERVER_PORT $IWARS_USER@$IWARS_MON &
echo "$!" > /tmp/iwars.ps.pid

# tunnel for RMI
xterm -e ssh -L 1098:$IWARS_MON:1098 $IWARS_USER@$IWARS_MON &
echo "$!" > /tmp/iwars.rmi1.pid

# tunnel for RMI
xterm -e ssh -L 1099:localhost:1099 $IWARS_USER@$IWARS_MON &
echo "$!" > /tmp/iwars.rmi2.pid

