#!/usr/bin/env bash

SCRIPT_DIR=$(dirname "$(readlink -f -- ${BASH_SOURCE[0]})")

#TODO should this be a param or config'ed some other way?
batchesFolder="$1"

complete="transfer_complete"
ack="transfer_acknowledged"

batchesReady=$(ls -1 "$batchesFolder"/*/"$complete" 2>/dev/null | sort -u | xargs -r -i dirname {} | xargs -r -i bash -c "[ -e '{}/$ack' ] || echo {}")

OLDIFS="$IFS"
IFS=$'\n'       # make newlines the only separator
for batch in $batchesReady; do
  flock --nonblock \
        --conflict-exit-code 0 \
        "/tmp/$(basename '$batch').lockfile" \
        -c "$HOME/AvisScanQA_cli/bin/performCheck.sh '$batch'"
done
IFS="$OLDIFS" # reset
