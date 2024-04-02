#!/bin/bash

# config file is first arg, logfile for the watchdog the second

# Function to start the fcserver process
start_fcserver() {
    /home/pi/fcserver "$1" > /home/pi/fcserver.log 2>&1 &
}

# Function to check if the fcserver process is running
is_fcserver_running() {
    pgrep -x "fcserver" > /dev/null
}

timestamp=$(date "+%Y-%m-%d %H:%M:%S")
echo "$timestamp - starting fcserver with watchdog"

# Start the fcserver process initially
start_fcserver "$1"

# Watchdog loop
while true; do
    if ! is_fcserver_running; then
        timestamp=$(date "+%Y-%m-%d %H:%M:%S")
        echo "$timestamp - fcserver process is not running. Restarting"
        start_fcserver "$1"
    fi
    sleep 2  # Check every 2 seconds
done
