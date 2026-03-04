#!/usr/bin/env python3
import os, textwrap

# GANTI ini ke package lama kamu yang benar
PACKAGE = "com.codedroid.ide"   # <-- kalau beda, ganti!
ROOT = os.path.abspath(".")

def d(s): return textwrap.dedent(s)

def write(rel, content):
    path = os.path.join(ROOT, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)
    print("write:", rel)

def main():
    # 1) Paksa app/build.gradle jadi versi bersih (biar gak error "mutate dependencies" lagi)
    write("app/build.gradle", d(f"""\
    plugins {{
        id "com.android.application"
    }}

    android {{
        namespace "{PACKAGE}"
        compileSdk 34

        defaultConfig {{
            applicationId "{PACKAGE}"
            minSdk 26
            targetSdk 34
            versionCode 1
            versionName "1.0"
        }}

        buildTypes {{
            release {{
                minifyEnabled false
            }}
        }}

        compileOptions {{
            sourceCompatibility JavaVersion.VERSION_17
            targetCompatibility JavaVersion.VERSION_17
            coreLibraryDesugaringEnabled true
        }}
    }}

    dependencies {{
        implementation "androidx.appcompat:appcompat:1.6.1"
        implementation "androidx.core:core:1.12.0"
        implementation "com.google.android.material:material:1.11.0"

        implementation "io.github.Rosemoe.sora-editor:editor:0.23.4"
        implementation "io.github.Rosemoe.sora-editor:language-textmate:0.23.4"

        coreLibraryDesugaring "com.android.tools:desugar_jdk_libs:2.0.4"
    }}
    """))

    # 2) Assets TextMate (grammar minimal) — cukup buat highlight Lua + JSON
    write("app/src/main/assets/textmate/languages.json", d("""\
    {
      "languages": [
        { "id": "lua",  "scopeName": "source.lua",  "extensions": [".lua", ".luau"] },
        { "id": "json", "scopeName": "source.json", "extensions": [".json"] }
      ],
      "grammars": [
        { "language": "lua",  "scopeName": "source.lua",  "path": "grammars/lua.tmLanguage.json" },
        { "language": "json", "scopeName": "source.json", "path": "grammars/json.tmLanguage.json" }
      ],
      "theme": { "path": "themes/codedroid-dark.json" }
    }
    """))

    # Lua grammar minimal (keyword/string/comment/number)
    write("app/src/main/assets/textmate/grammars/lua.tmLanguage.json", d(r"""\
    {
      "name": "Lua",
      "scopeName": "source.lua",
      "patterns": [
        { "name": "comment.line.lua", "match": "--.*$" },
        { "name": "string.quoted.double.lua", "begin": "\"", "end": "\"", "patterns": [ { "match": "\\\\." } ] },
        { "name": "string.quoted.single.lua", "begin": "'", "end": "'", "patterns": [ { "match": "\\\\." } ] },
        { "name": "constant.numeric.lua", "match": "\\b\\d+(\\.\\d+)?\\b" },
        { "name": "keyword.control.lua", "match": "\\b(and|break|do|else|elseif|end|false|for|function|if|in|local|nil|not|or|repeat|return|then|true|until|while)\\b" }
      ]
    }
    """))

    # JSON grammar minimal (string/number/boolean/null)
    write("app/src/main/assets/textmate/grammars/json.tmLanguage.json", d(r"""\
    {
      "name": "JSON",
      "scopeName": "source.json",
      "patterns": [
        { "name": "string.quoted.double.json", "begin": "\"", "end": "\"", "patterns": [ { "match": "\\\\." } ] },
        { "name": "constant.numeric.json", "match": "-?\\b\\d+(\\.\\d+)?([eE][+-]?\\d+)?\\b" },
        { "name": "constant.language.json", "match": "\\b(true|false|null)\\b" }
      ]
    }
    """))

    # Theme minimal (gelap)
    write("app/src/main/assets/textmate/themes/codedroid-dark.json", d("""\
    {
      "name": "CodeDroid Dark",
      "type": "dark",
      "colors": {
        "editor.background": "#0B0F17",
        "editor.foreground": "#D6E1FF"
      },
      "tokenColors": [
        { "scope": ["comment"], "settings": { "foreground": "#6B7A99" } },
        { "scope": ["string"],  "settings": { "foreground": "#9AE6B4" } },
        { "scope": ["keyword"], "settings": { "foreground": "#7AA2FF" } },
        { "scope": ["constant.numeric"], "settings": { "foreground": "#F6C177" } },
        { "scope": ["constant.language"], "settings": { "foreground": "#FF7AA2" } }
      ]
    }
    """))

    # 3) Tambah helper Java untuk apply TextMate ke editor
    pkg_path = PACKAGE.replace(".", "/")
    write(f"app/src/main/java/{pkg_path}/TextMateHelper.java", d(f"""\
    package {PACKAGE};

    import android.content.Context;

    import java.io.InputStream;

    import io.github.rosemoe.sora.langs.textmate.TextMateLanguage;
    import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry;
    import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver;
    import io.github.rosemoe.sora.langs.textmate.registry.provider.FileResolver;
    import io.github.rosemoe.sora.langs.textmate.registry.provider.IFileResolver;
    import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry;
    import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry;
    import io.github.rosemoe.sora.widget.CodeEditor;

    import org.json.JSONObject;

    public final class TextMateHelper {{

        private static boolean inited = false;

        public static void init(Context ctx) {{
            if (inited) return;
            try {{
                // register assets resolver
                IFileResolver resolver = new AssetsFileResolver(ctx.getAssets(), "textmate");
                FileProviderRegistry.getInstance().addFileProvider(resolver);

                // load config languages.json
                InputStream is = ctx.getAssets().open("textmate/languages.json");
                byte[] buf = new byte[is.available()];
                is.read(buf);
                is.close();
                String json = new String(buf);

                JSONObject root = new JSONObject(json);

                // theme
                String themePath = root.getJSONObject("theme").getString("path");
                ThemeRegistry.getInstance().loadTheme(themePath);

                // grammars
                GrammarRegistry.getInstance().loadGrammars(root);

                inited = true;
            }} catch (Exception ignored) {{
                inited = true; // avoid looping
            }}
        }}

        public static void applyByExt(Context ctx, CodeEditor editor, String ext) {{
            init(ctx);
            String scope = null;
            if (ext == null) ext = "";
            ext = ext.toLowerCase();

            if (ext.equals("lua") || ext.equals("luau")) scope = "source.lua";
            else if (ext.equals("json")) scope = "source.json";

            if (scope != null) {{
                editor.setEditorLanguage(TextMateLanguage.create(scope, true));
                ThemeRegistry.getInstance().applyTheme(editor);
            }} else {{
                editor.setEditorLanguage(null); // plain text
            }}
        }}
    }}
    """))

    print("\nDONE ✅")
    print("Next: jalankan git add/commit/push lalu build di Actions.")

if __name__ == "__main__":
    main()
