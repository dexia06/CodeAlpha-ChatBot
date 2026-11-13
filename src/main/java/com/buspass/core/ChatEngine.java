package com.buspass.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

/**
 * Retrieval-based chatbot. Each intent has predefined input patterns and a canned answer.
 * A query is normalised (lower-case, stop-words removed, stemmed, synonyms folded) and compared with every
 * pattern using the Dice coefficient; the best-scoring intent above the threshold wins.
 */
public final class ChatEngine {
    public record Result(String intent, String reply, double confidence) {}
    private record Intent(String name, List<Set<String>> patterns, String response) {}

    private static final double THRESHOLD = 0.4;
    static final String FALLBACK = "Sorry, I didn't catch that. You can ask me about booking, fares, routes, "
            + "tickets, your account or contacting support.";

    private static final Set<String> STOP = Set.of("a", "an", "the", "is", "are", "am", "to", "of", "in", "on", "for",
            "and", "or", "do", "does", "i", "me", "my", "you", "your", "it", "can", "please", "this", "that", "with",
            "at", "be", "there", "what", "how", "will", "would", "could", "should", "if", "want", "need", "get", "so");

    private static final Map<String, String> SYN = Map.ofEntries(
            Map.entry("cost", "price"), Map.entry("fare", "price"), Map.entry("pric", "price"),
            Map.entry("charge", "price"), Map.entry("amount", "price"), Map.entry("fee", "price"),
            Map.entry("rate", "price"), Map.entry("robot", "bot"),
            Map.entry("seat", "book"), Map.entry("reserve", "book"), Map.entry("buy", "book"),
            Map.entry("purchase", "book"), Map.entry("reservation", "book"),
            Map.entry("registration", "register"), Map.entry("signup", "register"),
            Map.entry("log", "login"), Map.entry("signin", "login"), Map.entry("logon", "login"),
            Map.entry("lose", "lost"), Map.entry("missing", "lost"), Map.entry("miss", "lost"),
            Map.entry("stole", "stolen"), Map.entry("steal", "stolen"), Map.entry("theft", "stolen"),
            Map.entry("thief", "stolen"), Map.entry("fake", "stolen"),
            Map.entry("pay", "payment"), Map.entry("paid", "payment"), Map.entry("upi", "payment"),
            Map.entry("card", "payment"), Map.entry("cancell", "cancel"), Map.entry("refund", "cancel"),
            Map.entry("buse", "bus"), Map.entry("genuine", "authentic"), Map.entry("valid", "authentic"),
            Map.entry("real", "authentic"), Map.entry("hello", "hi"), Map.entry("hey", "hi"));

    private final List<Intent> intents;

    private ChatEngine(List<Intent> intents) { this.intents = intents; }

    /** KB format, one intent per line: name|pattern1;pattern2;...|response  (lines starting with # are ignored). */
    public static ChatEngine load(Reader reader) throws IOException {
        List<Intent> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(reader)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.strip();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] p = line.split("\\|", 3);
                if (p.length < 3) throw new IOException("Bad knowledge-base line: " + line);
                List<Set<String>> pats = new ArrayList<>();
                for (String s : p[1].split(";")) {
                    Set<String> t = tokens(s);
                    if (!t.isEmpty()) pats.add(t);
                }
                list.add(new Intent(p[0].strip(), pats, p[2].strip()));
            }
        }
        if (list.isEmpty()) throw new IOException("Empty knowledge base");
        return new ChatEngine(list);
    }

    public Result answer(String query) {
        Set<String> q = tokens(query == null ? "" : query);
        Intent best = null;
        double bestScore = 0;
        for (Intent in : intents) {
            for (Set<String> pat : in.patterns()) {
                double s = score(q, pat);
                if (s > bestScore) { bestScore = s; best = in; }
            }
        }
        if (best == null || bestScore < THRESHOLD) return new Result("fallback", FALLBACK, bestScore);
        return new Result(best.name(), best.response(), bestScore);
    }

    static double score(Set<String> q, Set<String> pat) {
        if (q.isEmpty() || pat.isEmpty()) return 0;
        int inter = 0;
        for (String t : pat) if (q.contains(t)) inter++;
        if (inter == 0) return 0;
        double dice = 2.0 * inter / (q.size() + pat.size());
        // the whole multi-word pattern is contained in a longer question
        if (pat.size() >= 2 && inter == pat.size()) dice = Math.max(dice, 0.75);
        return dice;
    }

    static Set<String> tokens(String text) {
        Set<String> out = new LinkedHashSet<>();
        for (String w : text.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ").trim().split(" ")) {
            if (w.isEmpty() || STOP.contains(w)) continue;
            out.add(stem(w));
        }
        if (out.isEmpty()) { // question made only of common words, e.g. "what can you do"
            for (String w : text.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ").trim().split(" "))
                if (!w.isEmpty()) out.add(stem(w));
        }
        return out;
    }

    static String stem(String w) {
        if (w.endsWith("ies") && w.length() > 4) w = w.substring(0, w.length() - 3) + "y";
        else if (w.endsWith("s") && !w.endsWith("ss") && w.length() > 3) w = w.substring(0, w.length() - 1);
        if (w.endsWith("ing") && w.length() > 5) w = w.substring(0, w.length() - 3);
        else if (w.endsWith("ed") && w.length() > 4) w = w.substring(0, w.length() - 2);
        return SYN.getOrDefault(w, w);
    }
}
