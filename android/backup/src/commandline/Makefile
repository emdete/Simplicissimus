backup.tar.xz: backup.tar
	xz backup.tar
	rm backup.ab

Decompress.class: Decompress.java
	ant -emacs

backup.ab: Makefile
	adb backup -all -apk -obb -shared -nosystem

backup.tar: backup.ab Decompress.class
	ant -emacs run


