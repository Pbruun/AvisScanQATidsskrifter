#!/usr/bin/env bash

SCRIPT_DIR=$(dirname "$(readlink -f -- "${BASH_SOURCE[0]}")")

set -e
set -x
#port range 9010-9019
#9010: Tomcat shutdown
#9011: Tomcat http
#9019: tomcat debug

develServer=AvisScanQATidsskrifter@localhost
user="pabr"
devel="$user@${develServer}"
projectName=$(basename "$SCRIPT_DIR")
projectBaseUrl=$projectName
tomcatHttpPort=9631
tomcatDebugPort=9639
build=fast
version=$(mvn -Psbprojects-nexus  org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version -Psbforge-nexus | sed -n -e '/^\[.*\]/ !{ /^[0-9]/ { p; q } }')

echo $version
#Build

if [ $build == "fast" ]; then
    #Fast
    (
        mvn $1 package -Psbprojects-nexus -DskipTests=true
    ) || exit 1
elif [ $build != "not" ]; then
    # Extensive
    (
        cd "$SCRIPT_DIR/"..
        pwd
        mvn $1 package -Psbprojects-nexus -DskipTests=true --also-make --projects "$(basename "$SCRIPT_DIR")"
    ) || exit 1
fi

set -x
#install
rsync -av "$SCRIPT_DIR/target/${projectName}-${version}-package.tar.gz" "${devel}:."
ssh "${devel}" "tar -xvzf ./${projectName}-${version}-package.tar.gz"

rsync -av "$SCRIPT_DIR/conf/server/" "${devel}:services/conf/"
rsync -av "$SCRIPT_DIR/conf/server/${projectBaseUrl}.xml" "${devel}:tomcat/conf/Catalina/localhost/${projectBaseUrl}.xml"

echo "Stopping tomcat"
ssh "${devel}"  "(source .bash_profile && ~/bin/\$USER-tomcat stop | grep 'tomcat is not running') || sleep 10"
echo "Tomcat stopped"

ssh "${devel}" "rm -f ~/cache/*"

echo "Starting tomcat"
ssh "${devel}" "(source .bash_profile; export JPDA_ADDRESS='0.0.0.0:${tomcatDebugPort}'; ~/bin/\$USER-tomcat jpda start)"



echo "${projectName}.war deployed to ${develServer}:  http://${develServer}:${tomcatHttpPort}/${projectBaseUrl}"
