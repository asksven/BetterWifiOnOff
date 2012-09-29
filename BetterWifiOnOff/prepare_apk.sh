#!/bin/bash

VERSION=`cat AndroidManifest.xml | grep -o "versionName=\".*\"" | sed 's/"/ /g' | awk {'print $2'}`
echo "Detected Version $VERSION"
cp ./bin/BetterWifiOnOff-release.apk ./BetterWifiOnOff_$VERSION.apk
