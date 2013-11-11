#!/usr/bin/env bash

bin=`dirname ${BASH_SOURCE-$0}`
bin=`cd $bin;pwd -P`

exec $bin/bigdata-daemon.sh stop wordseg
