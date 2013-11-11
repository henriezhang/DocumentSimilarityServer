#!/usr/bin/env bash

bin=`dirname ${BASH_SOURCE-$0}`
bin=`cd $bin;pwd -P`

. ${bin}/bigdata-config.sh


if [[ $# != 1 ]]; then    
   echo "Usage: startup.sh [port]"
   exit 1
fi

exec $bin/bigdata-daemon.sh start similaryserver "$@"
