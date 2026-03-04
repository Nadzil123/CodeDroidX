import re

file = "app/build.gradle"

with open(file, "r") as f:
    data = f.read()

# Tambah coreLibraryDesugaringEnabled
if "coreLibraryDesugaringEnabled" not in data:
    data = re.sub(
        r"compileOptions\s*\{",
        """compileOptions {
        coreLibraryDesugaringEnabled true""",
        data
    )

# Tambah dependency desugaring
if "desugar_jdk_libs" not in data:
    data = data.replace(
        "dependencies {",
        """dependencies {
    coreLibraryDesugaring 'com.android.tools:desugar_jdk_libs:2.0.4'
"""
    )

with open(file, "w") as f:
    f.write(data)

print("Gradle fixed!")
