#!/system/bin/sh
MODDIR=${0%/*}

STATUS_FILE="$MODDIR/status"
[ ! -f "$STATUS_FILE" ] && exit 0

systemTrustedCerts=$(grep '^systemTrustedCerts=' "$STATUS_FILE" | cut -d'=' -f2)
adbOverNetwork=$(grep '^adbOverNetwork=' "$STATUS_FILE" | cut -d'=' -f2)

if [ "$systemTrustedCerts" = "true" ]; then
    CERT_SRC="/data/misc/user/0/cacerts-added"
    CERT_DST="$MODDIR/system/etc/security/cacerts"

    mkdir -p "$CERT_DST"
    rm -f "$CERT_DST"/*
    cp -f "$CERT_SRC"/* "$CERT_DST/" 2>/dev/null
fi

if [ "$adbOverNetwork" = "true" ]; then
    resetprop -n service.adb.tcp.port 5555
    stop adbd
    start adbd
elif [ "$adbOverNetwork" = "false" ]; then
    resetprop -n service.adb.tcp.port ""
    stop adbd
    start adbd
fi