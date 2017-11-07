It's Down
=========

Android learning code
---------------------

The sub-projects do nothing special or secret, all stuff probably is available
at google in a better or nicer shape. But i don't like these examples as they
mix alot of stuff. Goal of this collection is to concentrate on the topic. An
app that reads contacts just reads contacts, there is no gui, nothing (you get
a full-screen button to start reading). I don't even use string replacement in
resources and resolution dependend icons. The only feature that is used on top
if the topic is androids logging. So if the contacts are read they will be
written to the log and read by logcat.

How to use
----------

To actually see what code is needed to solve a problem just do a recursive diff
with the "template" subproject. This project is the bare base for all others
and with a diff you'll see what's needed to (for example) read contacts. All
the boilerplate needed for Android is the same in all subprojects.

The sub-projects play around with some Android features
-------------------------------------------------------

 - reading and writing contacts in a more java-typical manner (using enumerations)

 - receiving an acting on an intent

 - rescan media / refresh the cache

 - play around with messages

 - read nfc

 - write nfc

 - read qrcode

 - write qrcode

 - play around with a service and it's states

 - play around with tabs and the nice new gui elements

 - using openpgp via aidl

 - read wireless ids

and more will added as i start solving a new problem.

With all projects i try to

 - learn to use gradle (and use gradle in a multi-library/app project)

 - minimize code

 - optimize flows

