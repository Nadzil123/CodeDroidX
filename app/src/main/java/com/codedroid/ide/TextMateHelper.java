package com.codedroid.ide;

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

public final class TextMateHelper {

    private static boolean inited = false;

    public static void init(Context ctx) {
        if (inited) return;
        try {
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
        } catch (Exception ignored) {
            inited = true; // avoid looping
        }
    }

    public static void applyByExt(Context ctx, CodeEditor editor, String ext) {
        init(ctx);
        String scope = null;
        if (ext == null) ext = "";
        ext = ext.toLowerCase();

        if (ext.equals("lua") || ext.equals("luau")) scope = "source.lua";
        else if (ext.equals("json")) scope = "source.json";

        if (scope != null) {
            editor.setEditorLanguage(TextMateLanguage.create(scope, true));
            ThemeRegistry.getInstance().applyTheme(editor);
        } else {
            editor.setEditorLanguage(null); // plain text
        }
    }
}
