#!/usr/bin/env bash

# Court Case Scheduling Simulator (JuriBox)
# Cross-platform startup script for macOS and Linux

# Navigate to script directory
cd "$(dirname "$0")" || exit 1

echo "============================================================"
echo "      Court Case Scheduling Simulator (JuriBox)"
echo "============================================================"
echo ""

# Check for Java runtime
if ! command -v java >/dev/null 2>&1; then
    echo "[ERROR] 'java' command not found."
    echo "Please install Java JDK 8 or higher (e.g., via Homebrew: brew install openjdk)"
    echo ""
    read -n 1 -s -r -p "Press any key to exit..."
    echo ""
    exit 1
fi

mkdir -p bin
mkdir -p data
mkdir -p lib

echo "[1/2] Compiling Java source files..."
javac -cp "lib/*" -d bin src/juribox/exception/*.java src/juribox/model/*.java src/juribox/service/*.java src/juribox/app/*.java
COMPILE_STATUS=$?

if [ $COMPILE_STATUS -ne 0 ]; then
    echo "[ERROR] Compilation failed. Please inspect the error messages above."
    read -n 1 -s -r -p "Press any key to exit..."
    echo ""
    exit $COMPILE_STATUS
fi

echo "[2/2] Launching JuriBox application..."
echo ""
java -cp "bin:lib/*" juribox.app.Main

echo ""
echo "============================================================"
echo "Application session finished."
echo "============================================================"
