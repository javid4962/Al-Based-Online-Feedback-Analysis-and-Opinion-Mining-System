package com.datavalley.ml.pipeline;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class TextPreprocessor {

    private static final Set<String> PRESERVE = Set.of("not", "never", "no", "very", "extremely");

    private static final Set<String> STOPWORDS = new HashSet<>(Arrays.asList(
        "a", "an", "the", "and", "or", "but", "is", "am", "are", "was", "were", "be", "been",
        "to", "of", "for", "at", "in", "on", "by", "with", "this", "that", "it", "as", "from",
        "you", "your", "we", "our", "they", "their", "he", "she", "his", "her"
    ));

    public String normalize(String text) {
        if (text == null) {
            return "";
        }
        String value = text.toLowerCase(Locale.ROOT);
        value = value.replaceAll("https?://\\S+", " ");
        value = value.replaceAll("<[^>]+>", " ");
        value = value.replaceAll("[^a-z0-9\\s']", " ");
        value = value.replaceAll("\\s+", " ").trim();
        return value;
    }

    public List<String> tokenize(String text) {
        return Arrays.stream(normalize(text).split("\\s+"))
            .filter(token -> !token.isBlank())
            .filter(token -> !STOPWORDS.contains(token) || PRESERVE.contains(token))
            .collect(Collectors.toList());
    }
}
