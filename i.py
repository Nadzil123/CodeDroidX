cd ~/CodeDroidX

# paksa namespace jadi com.codedroidx.editor
sed -i 's/namespace ".*"/namespace "com.codedroidx.editor"/' app/build.gradle

# paksa applicationId jadi com.codedroidx.editor
sed -i 's/applicationId ".*"/applicationId "com.codedroidx.editor"/' app/build.gradle

# paksa package semua file java jadi com.codedroidx.editor
sed -i 's/^package .*/package com.codedroidx.editor;/' app/src/main/java/com/codedroidx/editor/*.java
