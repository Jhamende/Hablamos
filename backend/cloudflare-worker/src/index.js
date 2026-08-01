export default {
  async fetch(request, env) {
    if (request.method === "OPTIONS") return new Response(null, { headers: cors() });
    if (request.method !== "POST") return json({ error: "Method not allowed" }, 405);
    if (!env.OPENAI_API_KEY) return json({ error: "OPENAI_API_KEY is missing" }, 500);

    const body = await request.json();
    const level = body.level || "A1";
    const goal = body.goal || "Converser";
    const messages = Array.isArray(body.messages) ? body.messages.slice(-12) : [];
    const transcript = messages.map(m => `${m.role}: ${m.text}`).join("\n");

    const prompt = `Tu es le coach d'espagnol de l'application Hablamos. Niveau CECRL: ${level}. Objectif: ${goal}.
Conduis une conversation naturelle principalement en espagnol. Adapte la longueur et le vocabulaire au niveau.
Corrige seulement les erreurs utiles, explique brièvement en français et propose une réponse possible.
Réponds uniquement en JSON valide avec: reply, correction, explanation, suggestedReply, xp.
Conversation:\n${transcript}`;

    const response = await fetch("https://api.openai.com/v1/responses", {
      method: "POST",
      headers: {
        "Authorization": `Bearer ${env.OPENAI_API_KEY}`,
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        model: env.OPENAI_MODEL || "gpt-5-mini",
        input: prompt,
        text: { format: { type: "json_object" } }
      })
    });

    if (!response.ok) return json({ error: await response.text() }, response.status);
    const data = await response.json();
    const output = data.output_text || data.output?.flatMap(x => x.content || []).find(x => x.text)?.text;
    try {
      return json(JSON.parse(output));
    } catch {
      return json({ reply: output || "Lo siento, no he podido responder.", correction: "", explanation: "", suggestedReply: "", xp: 5 });
    }
  }
};

function cors() {
  return { "Access-Control-Allow-Origin": "*", "Access-Control-Allow-Headers": "Content-Type", "Access-Control-Allow-Methods": "POST, OPTIONS" };
}
function json(value, status = 200) {
  return new Response(JSON.stringify(value), { status, headers: { "Content-Type": "application/json", ...cors() } });
}
