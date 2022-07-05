#!/bin/sh
#
mv err.log log/err_$(date -d "today" +"%Y%m%d%H%M").log
mv out.log log/out_$(date -d "today" +"%Y%m%d%H%M").log
#
sudo java -cp bin/cwvaServer-2.0.1.jar:bin/lsys-2.0.1.jar cwva.Main -cfg serverCwva2.rson >out.log 2>err.log &

