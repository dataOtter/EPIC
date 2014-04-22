#!/bin/csh -f

setenv LOC $1
setenv DIR .git

if ($LOC == "out") then
cat $DIR/config | sed -e 's/\(.*@\)10.2.41.58\(.*\)/\1fclab.jjay.cuny.edu\2/' > $DIR/config.new
mv $DIR/config $DIR/config.backup
mv $DIR/config.new $DIR/config
else if ($LOC == "in") then
cat $DIR/config | sed -e 's/\(.*@\)fclab.jjay.cuny.edu\(.*\)/\110.2.41.58\2/' > $DIR/config.new
mv $DIR/config $DIR/config.backup
mv $DIR/config.new $DIR/config
else
echo "Usage: $0 in|out"
endif
endif 
