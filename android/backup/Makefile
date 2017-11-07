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
	rm -f backup.* *.class

