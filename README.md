# Prometheus

AfrikaBurn 2018

A collection of projects:

- `grid-gen`: tool/visualisation used to generate the grid used.
ClojureScript project.
- `ring-demo`: simple processing demo, using the JSON layout file.
- `SyphonOPC`: bridge app for Syphon.

## Simulator

For dev/testing, use the [openpixelcontrol][opc]'s handy `gl_server`.
as a simulator.

    ./bin/gl_server -l path/to/layout.json

[opc]: https://github.com/zestyping/openpixelcontrol

## Pi Setup

> Updated for 2024

We are using Raspberry Pi 3b+, with a PoE hat.

For all the the below, we use `w0`. repeat for `w1` and `w2` (spare pi)

Create the image Use Raspberry Pi Imager (currently v1.8.5)

* Device -> Pi 3; OS Legacy 32-bit Lite (Bullseye)
* Set hostname to `wing-0.local`
* Set username and password to `pi:<password>`
* Enable SSH (password)

Flash image. Connect to a router and wait for it to boot up.

copy these two files over (local path might vary)

```
scp code/prometheus/config/w0-fcserver.json pi@wing-0.local:w0-fcserver.json
scp code/prometheus/config/fcserver_watchdog.sh pi@wing-0.local:fcserver_watchdog.sh
scp code/fadecandy/bin/fcserver-rpi pi@wing-0.local:fcserver
```

SSH in (`ssh pi@wing-0.local`) and verify that you can run it

```
sudo ./fcserver w0-fcserver.json
```

to automatically start it, add

```
/home/pi/fcserver_watchdog.sh /home/pi/w0-fcserver.json &
```

to `/etc/rc.local`, before `exit 0`

then

```
sudo reboot
```

wait about two minutes. verify that you can connect to web interface.
