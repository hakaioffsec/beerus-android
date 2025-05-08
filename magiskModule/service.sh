#!/system/bin/sh
MODDIR=${0%/*}

STATUS_FILE="$MODDIR/status"
[ ! -f "$STATUS_FILE" ] && exit 0

fridaProp=$(grep '^frida=' "$STATUS_FILE" | cut -d'=' -f2)
fridaBin="/data/local/tmp/hiddenBin"

if [ "$fridaProp" = "true" ] && [ -x "$fridaBin" ]; then
    "$fridaBin" &
fi

# Wait for boot to complete
until [ "$(getprop sys.boot_completed)" -eq 1 ]; do
    sleep 5
done

"beerusd" >> "$MODDIR/beerusd.log" 2>&1 &