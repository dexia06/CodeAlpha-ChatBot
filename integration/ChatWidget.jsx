import React, { useEffect, useRef, useState } from 'react';
import { api } from './api.js';

const CHIPS = ['How do I book?', 'Show routes', 'Lost my ticket', 'Is my ticket safe?'];

export default function ChatWidget() {
  const [open, setOpen] = useState(false);
  const [text, setText] = useState('');
  const [busy, setBusy] = useState(false);
  const [msgs, setMsgs] = useState([{ from: 'bot', text: "Hi! I'm the BusPass assistant. Ask me about booking, fares, routes or your tickets." }]);
  const end = useRef(null);

  useEffect(() => { end.current && end.current.scrollIntoView({ block: 'end' }); }, [msgs, open]);

  async function send(message) {
    const q = message.trim();
    if (!q || busy) return;
    setText('');
    setMsgs((m) => [...m, { from: 'me', text: q }]);
    setBusy(true);
    try {
      const r = await api('/api/chat', { method: 'POST', body: { message: q } });
      setMsgs((m) => [...m, { from: 'bot', text: r.reply }]);
    } catch (e) {
      setMsgs((m) => [...m, { from: 'bot', text: 'I could not reach the server. Please try again.' }]);
    } finally { setBusy(false); }
  }

  if (!open) return <button className="chat-fab" onClick={() => setOpen(true)} aria-label="Open chat assistant">Chat</button>;

  return (
    <section className="chat" aria-label="Chat assistant">
      <header><strong>BusPass assistant</strong><button onClick={() => setOpen(false)} aria-label="Close chat">&times;</button></header>
      <div className="chat-log" role="log" aria-live="polite">
        {msgs.map((m, i) => <p key={i} className={'bubble ' + m.from}>{m.text}</p>)}
        <div ref={end} />
      </div>
      <div className="chips">{CHIPS.map((c) => <button key={c} onClick={() => send(c)}>{c}</button>)}</div>
      <form onSubmit={(e) => { e.preventDefault(); send(text); }}>
        <input value={text} maxLength={300} onChange={(e) => setText(e.target.value)} placeholder="Type your question" aria-label="Your question" />
        <button type="submit" disabled={busy}>Send</button>
      </form>
    </section>
  );
}
