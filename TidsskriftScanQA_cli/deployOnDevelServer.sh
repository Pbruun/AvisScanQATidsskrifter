#!/usr/bin/env bash

SCRIPT_DIR=$(dirname "$(readlink -f -- ${BASH_SOURCE[0]})")

projectName=$(basename "$SCRIPT_DIR")
develServer=AvisScanQATidsskrifter@localhost



#Build
(cd "$SCRIPT_DIR/"..; pwd; mvn $1 package -Psbprojects-nexus -DskipTests=true --also-make --projects "$(basename "$SCRIPT_DIR")") || exit 1

#TODO derive version from pom file?
version=1.0-SNAPSHOT

echo "Uploding package to server"
rsync -a "$SCRIPT_DIR/target/${projectName}-${version}.tar.gz" "${develServer}:."

echo "Extracting package on server"
ssh "${develServer}" "tar -xzf ./${projectName}-${version}.tar.gz"

echo "Installing crontab to automatically check new batches"
ssh "${develServer}" "crontab -r; cat ${projectName}/conf/crontab | crontab -;"

echo "Done"
