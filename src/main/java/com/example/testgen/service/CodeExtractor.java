package com.example.testgen.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Nettoie la réponse du LLM : retire les balises Markdown ```java ... ```. */
public final class CodeExtractor {

    private static final Pattern FENCE = Pattern.compile("```(?:java)?\\s*\\n(.*?)```", Pattern.DOTALL);

    private CodeExtractor() {}

    public static String extractJava(String raw) {
        if (raw == null) return "";
        Matcher m = FENCE.matcher(raw);
        if (m.find()) {
            return m.group(1).trim();
        }
        return raw.trim();
    }
}
