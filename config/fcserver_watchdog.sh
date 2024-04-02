#!/bin/bash

# config file is first arg

# Function to start the fcserver process
start_fcserver() {
    /home/pi/fcserver "$1" > /home/pi/fcserver.log 2>&1 &
}

# Function to check if the fcserver process is running
is_fcserver_running() {
    pgrep -x "fcserver" > /dev/null
}

# Start the fcserver process initially
start_fcserver "$1"

# Watchdog loop
while true; do
    if ! is_fcserver_running; then
        echo "fcserver process is not running. Restarting..."
        start_fcserver "$1"
    fi
    sleep 2  # Check every 2 seconds
done &
