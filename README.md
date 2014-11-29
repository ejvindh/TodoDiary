TodoDiary - desktop
===================

Regain the power of your own life documentary!

TodoDiary combines your Calendar/Todo-list with a Diary. The entries in your Calendar/Todo-list are saved as ordinary txt-files that can be read on any device. Furthermore it is saved in Dropbox, which makes it possible for you to save it on your local drives, if you wish. This makes it possible for you to access your previous life and future in the database-files, and it will never disappear. Not even if you switch to another Calendar-system later on. Therefore it makes sense to have both ordinary calendar-entries and more reflective diary-scribles.

The application is written in Java, which makes it operationable in multiple platforms. In case you use Dropbox you can further supplement your approach with DBTodo on Android (also available on Github), which accesses the databases directly from Dropbox.
DBTodo: https://github.com/ejvindh/DBTodo

The desktop-client is not integrated with Dropbox, but if you have Dropbox installed on your desktop, you may simply direct the attention of the client to the Dropbox-folder in which the Android-app saves the databases (Dropbox/Apps/TodoDiary/), and then you have full integration between the systems!

Attention: Be carefull with the Raw-DB-View. Don't mess with the lines containing the date-indications. If they do not have the right format, TodoDiary will not be able to show and save content in the right places. And they may also risk becomming invisible in the "Single-Day-View".
	  
1) Download the .jar-file found here:
https://github.com/ejvindh/TodoDiary/releases
Run with "java -jar TodoDiary.jar"

Notice: A similar project can be found here: http://todotxt.com/
Several applications are created around this project. I'm not affiliated with that project, and my project has been developed independently.

---------------
v1.0: December 2014
- First public release

