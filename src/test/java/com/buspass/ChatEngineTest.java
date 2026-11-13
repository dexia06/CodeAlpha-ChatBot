package com.buspass;

import static org.junit.jupiter.api.Assertions.*;

import com.buspass.core.ChatEngine;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class ChatEngineTest {
    @Test
    void chatbotAccuracyAtLeast90Percent() throws Exception {
        ChatEngine engine;
        try (var in = new ClassPathResource("chatbot-kb.txt").getInputStream()) {
            engine = ChatEngine.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
        List<String> lines;
        try (var in = new ClassPathResource("chat-eval.tsv").getInputStream()) {
            lines = new String(in.readAllBytes(), StandardCharsets.UTF_8).lines().filter(l -> !l.isBlank()).toList();
        }
        int ok = 0;
        for (String l : lines) {
            String[] p = l.split("\t");
            if (engine.answer(p[0]).intent().equals(p[1])) ok++;
            else System.out.println("CHATBOT MISS: " + p[0]);
        }
        double acc = 100.0 * ok / lines.size();
        System.out.printf("CHATBOT ACCURACY: %d/%d = %.1f%%%n", ok, lines.size(), acc);
        assertTrue(acc >= 90.0, "accuracy " + acc);
        assertEquals("fallback", engine.answer("zxqv blorp").intent());
        assertEquals("fallback", engine.answer("").intent());
    }
}
