const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
  "Content-Type": "application/json; charset=utf-8",
};

type Dependencies = {
  apiKey: () => string | undefined;
  fetcher?: typeof fetch;
  now?: () => number;
  timeoutMs?: number;
};

function json(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), { status, headers: corsHeaders });
}

export function offerFromResult(result: Record<string, unknown>, position: number, query: string) {
  let sourceUrl = "";
  let store = typeof result.site_name === "string" ? result.site_name.trim() : "";
  try {
    const parsed = new URL(typeof result.url === "string" ? result.url : "");
    if (parsed.protocol === "http:" || parsed.protocol === "https:") {
      sourceUrl = parsed.toString();
      store ||= parsed.hostname.replace(/^www\./, "");
    }
  } catch { /* Un enlace inválido nunca se abre en el cliente. */ }
  return {
    id: `${position}-${sourceUrl || query}`,
    store: store || "Fuente web",
    productName: typeof result.title === "string" && result.title.trim() ? result.title : query,
    price: "Ver en fuente",
    originalPrice: null,
    shipping: "Consulta en tienda",
    delivery: "Disponibilidad por confirmar",
    rating: "—",
    sourceUrl,
    summary: typeof result.snippet === "string" ? result.snippet : "Resultado encontrado en la web.",
    tags: ["Resultado web"],
    isBestValue: false,
  };
}

// Admite una clave sola o copiada con su etiqueta/prompt. El texto nunca se ejecuta.
export function normalizeApiKey(raw: string | undefined): string | undefined {
  const value = raw?.trim();
  if (!value) return undefined;
  const keys = [...new Set(value.match(/sk-tinyfish-[A-Za-z0-9_-]+/g) ?? [])];
  if (keys.length > 1) return undefined;
  if (keys.length === 1) return keys[0];
  return /[\r\n]/.test(value) ? undefined : value;
}

export function createSearchHandler({
  apiKey, fetcher = fetch, now = Date.now, timeoutMs = 15_000,
}: Dependencies) {
  // Límite didáctico por instancia. No sustituye cuotas compartidas de producción.
  const requestTimes = new Map<string, number[]>();
  function isRateLimited(request: Request) {
    const timestamp = now();
    for (const [key, times] of requestTimes) {
      if (times.every((time) => time <= timestamp - 60_000)) requestTimes.delete(key);
    }
    const key = request.headers.get("x-forwarded-for")?.split(",")[0]?.trim() || "anonymous";
    const recent = (requestTimes.get(key) ?? []).filter((time) => time > timestamp - 60_000);
    if (recent.length >= 20) return true;
    // Acota memoria: clientes nuevos comparten un grupo si la instancia está saturada.
    const bucket = requestTimes.has(key) || requestTimes.size < 10_000 ? key : "overflow";
    const bucketTimes = bucket === key ? recent : (requestTimes.get(bucket) ?? []).filter((time) => time > timestamp - 60_000);
    if (bucketTimes.length >= 20) return true;
    requestTimes.set(bucket, [...bucketTimes, timestamp]);
    return false;
  }

  return async (request: Request): Promise<Response> => {
    if (request.method === "OPTIONS") return new Response(null, { status: 204, headers: corsHeaders });
    if (request.method !== "POST") return json({ error: "Usa una solicitud POST para buscar." }, 405);
    if (isRateLimited(request)) return json({ error: "Demasiadas búsquedas. Inténtalo en un minuto." }, 429);

    let query = "";
    try {
      const body = await request.json();
      query = typeof body?.query === "string" ? body.query.trim() : "";
    } catch {
      return json({ error: "El cuerpo debe ser JSON válido." }, 400);
    }
    if (query.length < 2 || query.length > 200) {
      return json({ error: "La búsqueda debe tener entre 2 y 200 caracteres." }, 400);
    }
    const key = normalizeApiKey(apiKey());
    if (!key) return json({ code: "TINYFISH_NOT_CONFIGURED", error: "La búsqueda en vivo aún no está configurada." }, 503);

    const searchUrl = new URL("https://api.search.tinyfish.ai");
    searchUrl.searchParams.set("query", query);
    searchUrl.searchParams.set("purpose", "Encontrar páginas de compra y ofertas relevantes.");
    searchUrl.searchParams.set("location", "EC");
    searchUrl.searchParams.set("language", "es");
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), timeoutMs);
    try {
      const response = await fetcher(searchUrl, {
        headers: { "X-API-Key": key, Accept: "application/json" },
        signal: controller.signal,
      });
      if (!response.ok) {
        console.error("TinyPrice upstream", { kind: "http", status: response.status });
        await response.body?.cancel();
        return json({ code: "TINYFISH_HTTP_ERROR", error: "No fue posible consultar las fuentes web ahora." }, 502);
      }
      const payload = await response.json();
      if (!payload || !Array.isArray(payload.results)) {
        return json({ code: "TINYFISH_INVALID_RESPONSE", error: "Las fuentes web devolvieron una respuesta no válida." }, 502);
      }
      const results = payload.results.filter((item: unknown): item is Record<string, unknown> =>
        item !== null && typeof item === "object" && !Array.isArray(item)
      );
      return json({
        query,
        offers: results.slice(0, 10).map((result: Record<string, unknown>, index: number) => offerFromResult(result, index + 1, query)),
        searchedAt: new Date(now()).toISOString(),
      });
    } catch (error) {
      // Solo categorías fijas: nunca registrar mensajes, URLs, cabeceras ni secretos.
      const description = error instanceof Error ? error.message.toLowerCase() : "";
      const kind = controller.signal.aborted ? "timeout"
        : error instanceof SyntaxError ? "invalid_json"
        : /header|invalid character/.test(description) ? "invalid_header"
        : /dns|lookup|resolve/.test(description) ? "dns"
        : /certificate|tls|ssl/.test(description) ? "tls"
        : "network";
      console.error("TinyPrice upstream", { kind });
      return json({ code: `TINYFISH_${kind.toUpperCase()}`, error: "La búsqueda web no está disponible en este momento." }, 502);
    } finally {
      clearTimeout(timeout);
    }
  };
}
