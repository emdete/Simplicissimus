.PHONY: all run dbg clean gradle

all:
	./gradlew -q assembleRelease

run:
	./gradlew -q assembleDebug

dbg: run
	#adb uninstall de.emdete.android.gui
	adb install -r build/outputs/apk/*-debug.apk
	adb shell am start de.emdete.android.gui/.Sample

clean:
	./gradlew -q clean

d:
	meld src/main/java/de.emdete/android/wirelessid ../../proprorep/src/main/java/de/emdete/android/wirelessid
	meld src/main/java/de.emdete/android/wirelessid ../../emdete-wirelesslocation/src/de/emdete/android/wirelessid
