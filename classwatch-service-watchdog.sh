#!/bin/sh

PIDFILE="/var/run/classwatch/classwatch-service.pid"
SERVICE="/home/alwold/classwatch-service/bin/classwatch-service"

if [ -e $PIDFILE ]; then
  PID=`cat $PIDFILE`
  ps -p $PID > /dev/null
  if [ "$?" != "0" ]; then
    nohup $SERVICE &
  fi
else
  nohup $SERVICE &
fi