# Task 4 - BusPass AI Chatbot (retrieval-based)

Spring Boot 3 / Java 21 service. Predefined question patterns (`src/main/resources/chatbot-kb.txt`, 18 intents) are matched to user questions with a normalise (stop-words, stemming, synonyms) + Dice-coefficient retrieval model. Instant replies, no external API.

- API: `POST /api/chat` with `{"message":"how do I book"}` returns `{"reply","intent","confidence"}`
- Demo page: `/` (clean chat UI). Integration with the Bus Pass site: see `integration/INTEGRATE_WITH_BUSPASS.md` (the combined `buspass.zip` already has it embedded).
- Run: `mvn spring-boot:run` then open http://localhost:8080
- Tests: `mvn verify` - accuracy test over 44 questions (must be >= 90%), API tests for answers, fallback, validation, health.
- Deploy: same Elastic Beanstalk steps as the Bus Pass README (no database or environment variables needed); CI uploads `bundle.zip`.
