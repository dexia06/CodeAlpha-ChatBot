package com.buspass;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ChatApiTest {
    @Autowired MockMvc mvc;

    private org.springframework.test.web.servlet.ResultActions ask(String m) throws Exception {
        return mvc.perform(post("/api/chat").contentType(MediaType.APPLICATION_JSON).content("{\"message\":\"" + m + "\"}"));
    }

    @Test void answersBookingQuestion() throws Exception { ask("how do I book a ticket").andExpect(status().isOk()).andExpect(jsonPath("$.intent").value("book")); }
    @Test void answersLostTicket() throws Exception { ask("I lost my ticket").andExpect(jsonPath("$.intent").value("lost")); }
    @Test void unknownGetsFallback() throws Exception { ask("zxqv blorp").andExpect(status().isOk()).andExpect(jsonPath("$.intent").value("fallback")); }
    @Test void emptyMessageRejected() throws Exception { ask("").andExpect(status().isBadRequest()); }
    @Test void healthUp() throws Exception { mvc.perform(get("/actuator/health")).andExpect(status().isOk()); }
    @Test void demoPageServed() throws Exception { mvc.perform(get("/")).andExpect(status().isOk()); }
}
