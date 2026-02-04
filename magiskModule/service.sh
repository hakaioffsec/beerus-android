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

# Remove toast for beerus
TARGET_UID=$(pm list packages -U | grep io.hakaisecurity.beerusframework | awk -F'uid:' '{print $2}' | tr -d '\r\n ')

if [ ! -z "$TARGET_UID" ]; then
    dbAgent /data/adb/magisk.db "UPDATE policies SET notification=0 WHERE uid=$TARGET_UID;"
    sqlite3 /data/adb/magisk.db "UPDATE policies SET notification=0 WHERE uid=$TARGET_UID;"
fi

# set namespace to global
dbAgent /data/adb/magisk.db "UPDATE settings SET value='0' WHERE key='mnt_ns';"
sqlite3 /data/adb/magisk.db "UPDATE settings SET value='0' WHERE key='mnt_ns';"

proxyProp=$(grep '^proxy=' "$STATUS_FILE" | cut -d'=' -f2)
settings put global http_proxy "$proxyProp"