#!/usr/bin/env bash
# Build the Helical MongoDB JDBC driver and install it into hi-repository/System/Drivers.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT/server/himongo-jdbc"
mvn clean package
cp -f target/himongo-jdbc-1.0.0.jar "$ROOT/server/hi-repository/System/Drivers/himongo-jdbc-1.0.0.jar"
echo "Installed: $ROOT/server/hi-repository/System/Drivers/himongo-jdbc-1.0.0.jar"
