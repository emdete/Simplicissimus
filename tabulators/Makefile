.PHONY: all run dbg clean gradle

all:
	./gradlew -q assembleRelease

run:
	./gradlew -q assembleDebug

dbg: run
	adb install -r build/apk/*-debug-unaligned.apk
	adb shell am start org.pyneo.android.gui/.Sample

clean:
	./gradlew -q clean

