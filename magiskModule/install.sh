AUTOMOUNT=true
SKIPMOUNT=false
PROPFILE=false
POSTFSDATA=true
LATESTARTSERVICE=true
REPLACE=""

print_modname(){
    ui_print "***********************************************************************"
    ui_print "Beerus Magisk Module :)"
    ui_print "***********************************************************************"
}

on_install(){
    unzip -o "$ZIPFILE" -d $MODPATH >&2
}

set_permissions(){
    set_perm_recursive $MODPATH 0 0 0755 0644
}