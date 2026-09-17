#!/usr/bin/env bash
set -euo pipefail
repository="${INSTALLATION_LOCATION}/hi/hi-repository"
mkdir -p "$repository"
if [ ! -f "$repository/System/Admin/setting.xml" ]; then
  cp -a /opt/hi-repository/. "$repository/"
fi
# Refresh the built driver while keeping saved connections and reports on the volume.
cp /opt/himongo-jdbc-1.0.0.jar "$repository/System/Drivers/"
exec /entrypoint.sh
