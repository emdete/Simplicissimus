.PHONY: all run dbg clean gradle

all:
	./gradlew -q assembleRelease

run:
	./gradlew -q assembleDebug

dbg: run
	#adb uninstall org.pyneo.android.gui
	adb install -r build/outputs/apk/*-debug.apk
	adb shell am start org.mapsforge.applications.android.samples/.Samples

clean:
	./gradlew -q clean

dl:
	# see http://download.mapsforge.org/maps/
	if [ ! -f germany.map ] ; then wget http://download.mapsforge.org/maps/europe/germany.map; fi
	adb push germany.map /storage/sdcard1/.
	adb push theme.xml /storage/sdcard1/.
