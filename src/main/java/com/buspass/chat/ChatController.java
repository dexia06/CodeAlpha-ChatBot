package com.buspass.chat;

import com.buspass.core.ChatEngine;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ChatController {
    public record ChatReq(@NotBlank @Size(max = 300) String message) {}
    public record ChatRes(String reply, String intent, double confidence) {}

    private final ChatEngine engine;

    public ChatController(ChatEngine engine) { this.engine = engine; }

    @PostMapping("/chat")
    public ChatRes chat(@Valid @RequestBody ChatReq r) {
        ChatEngine.Result res = engine.answer(r.message());
        return new ChatRes(res.reply(), res.intent(), res.confidence());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, String>> invalid(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest().body(Map.of("message", "Message must be 1 to 300 characters"));
    }
}
