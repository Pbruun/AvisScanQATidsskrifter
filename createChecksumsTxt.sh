#!/usr/bin/env bash

#Started with
# cd /home/avisscqa/data/avis-scanner-prod/_07-levering-fra-Ninestars/Shipment_2021-07-19
# set -e; for dir in *_RT*; do echo "$dir"; pushd "$dir";  createChecksumsTxt.sh; popd; done  &> checksums.log &

set -e
set -x

# Print current dir for debugging purposes
echo "$PWD"

#Must be in batch-dir

# Delete individuel md5 files
find . -name '*.md5' -print0 | xargs -0 -r -i rm '{}'

#Remove existing checksums.txt file so it does not get checksummed
rm -f checksums.txt

# Generate new checksums.txt file
find ./ \
    \( -type f -o -type l \
    -not -name 'checksums.txt' \) \
    -print0  | \
  sed -e 's/^/\x0/' \
      -e 's|\x0\./|\x0|g' \
      -e 's|^\x0||'  | \
  xargs \
    --null \
    --no-run-if-empty \
    -I'{}' \
    -P4 \
    md5sum -- '{}' \
  > checksums.txt

echo "done"


#Andre sjove bash ting
# find files modified 10 minutes or more ago
#find -mmin +10

# find files modified no more than 10 minutes ago
#find -mmin -10
