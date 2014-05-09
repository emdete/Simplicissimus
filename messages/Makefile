all:
	ANDROID_HOME=/usr/local/android/sdk \
	./gradlew -q assemble

run:
	ANDROID_HOME=/usr/local/android/sdk \
	./gradlew -q build

dbg: run
	/usr/local/android/sdk/platform-tools/adb install -r build/apk/*-debug-unaligned.apk
	adb shell am start org.pyneo.android.contacts/.Sample

clean:
	ANDROID_HOME=/usr/local/android/sdk \
	./gradlew -q clean

