.PHONY: all run dbg clean gradle

all:
	./gradlew -q assembleRelease

run:
	./gradlew -q assembleDebug

dbg: run
	#chmod 0644 build/outputs/apk/*-debug.apk
	#rsync --verbose --archive build/outputs/apk/*-debug.apk littlelun.emdete.de:/var/www/emdete.de/c/.
	#adb uninstall de.emdete.android.gui
	adb install -r build/outputs/apk/*-debug.apk
	adb shell am start de.emdete.android.gui/.Sample

clean:
	./gradlew -q clean

