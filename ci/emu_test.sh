#!/usr/bin/env bash
# يعرض الودجت على محاكي أندرويد 14 ويصوّره، ثم يلمس الصندوق ليرى فتح حوار المستشار.
P=net.wardyat.mustashari; O=out; mkdir -p $O; R=$O/result.txt; : > $R
log() { echo "$@" | tee -a $R; }
shot() { adb exec-out screencap -p > "$O/$1.png"; }
adb wait-for-device; adb shell settings put global window_animation_scale 0; adb shell settings put global transition_animation_scale 0; adb shell settings put global animator_duration_scale 0
adb install -r app.apk | tee -a $R
adb logcat -c
adb shell am start -n $P/.WidgetPreviewActivity >/dev/null; sleep 4; shot 01_widget
W=$(adb shell wm size | grep -o '[0-9]*x[0-9]*' | tail -1); log "شاشة $W"
tap_id() {
  adb shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1; adb pull /sdcard/ui.xml $O/ui.xml >/dev/null 2>&1
  python3 - "$1" <<'PY'
import re,sys
x=open('out/ui.xml',encoding='utf8',errors='ignore').read()
for m in re.finditer(r'<node[^>]*>',x):
    n=m.group(0)
    if sys.argv[1] in n:
        b=re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"',n)
        if b:
            a=list(map(int,b.groups())); print((a[0]+a[2])//2,(a[1]+a[3])//2); sys.exit(0)
sys.exit(1)
PY
}
XY=$(tap_id 'اكتب همّك'); log "موضع الصندوق: $XY"
FY=$(tap_id 'content-desc="التالي"'); log "موضع الوجه: $FY"
[ -n "$FY" ] && { adb shell input tap $FY; sleep 2; shot 02_next_persona; }
[ -n "$XY" ] && { adb shell input tap $XY; sleep 20; shot 03_chat_opened; }
adb logcat -d | grep -E "AndroidRuntime|FATAL|AskWidget" > $O/logcat.txt; tail -10 $O/logcat.txt | tee -a $R
