# Integrating this chatbot into the Bus Pass website (Task 3)

Option A (used in the combined project `buspass.zip`): the chatbot runs inside the Bus Pass Spring Boot app.
Copy `ChatEngine.java`, `chatbot-kb.txt`, `chat-eval.tsv` and the `/api/chat` controller method, add `ChatWidget.jsx` to the React app and render `<ChatWidget />` in `App.jsx`.

Option B (separate service): deploy this project as its own service, then in `ChatWidget.jsx` change the fetch URL
`'/api/chat'` to `'https://<your-chatbot-url>/api/chat'`. CORS is already enabled for `/api/chat` (restrict `allowedOrigins` in production).
