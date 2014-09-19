.PHONY: all run dbg clean gradle

all:
	./gradlew -q assembleRelease

run:
	./gradlew -q assembleDebug

dbg: run
	scp build/apk/*-debug-unaligned.apk littlelun.emdete.de:/var/www/pyneo.org/c/itsdown.apk
	#adb uninstall org.pyneo.android.gui
	adb install -r build/apk/*-debug-unaligned.apk
	adb shell am start org.pyneo.android.gui/.Sample

clean:
	./gradlew -q clean

