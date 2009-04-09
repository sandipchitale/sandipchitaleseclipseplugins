#!/bin/sh
BASEDIR=`dirname $0`
exec java -XstartOnFirstThread -classpath $BASEDIR/swt/:$BASEDIR/MoveResize.jar -Djava.library.path=$BASEDIR/swt moveresize.Main -server 6789
