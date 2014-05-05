all:
	ANDROID_HOME=/usr/local/android/sdk \
	./gradlew -q assemble

run:
	ANDROID_HOME=/usr/local/android/sdk \
	./gradlew -q build

dbg: run
	#adb uninstall org.pyneo.android.gui
	adb install -r build/apk/*-debug-unaligned.apk
	adb shell am start org.pyneo.android.gui/.SingleButtonTester

clean:
	ANDROID_HOME=/usr/local/android/sdk \
	./gradlew -q clean

