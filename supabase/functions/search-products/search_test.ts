import { createSearchHandler, offerFromResult, normalizeApiKey } from "./search.ts";

function equal(actual: unknown, expected: unknown) {
  if (JSON.stringify(actual) !== JSON.stringify(expected)) {
    throw new Error(`Expected ${JSON.stringify(expected)}, got ${JSON.stringify(actual)}`);
  }
}
const post = (body: unknown) => new Request("http://localhost/search-products", {
  method: "POST", headers: { "content-type": "application/json" }, body: JSON.stringify(body),
});
const unreachable: typeof fetch = () => { throw new Error("The provider must not be called"); };

Deno.test("CORS and method checks do not call TinyFish", async () => {
  const handle = createSearchHandler({ apiKey: () => undefined, fetcher: unreachable });
  const options = await handle(new Request("http://localhost", { method: "OPTIONS" }));
  equal(options.status, 204);
  equal(options.headers.get("access-control-allow-origin"), "*");
  equal((await handle(new Request("http://localhost"))).status, 405);
});

Deno.test("invalid JSON and all invalid query shapes return 400", async () => {
  const handle = createSearchHandler({ apiKey: () => "test-only", fetcher: unreachable });
  for (const body of [null, [], {}, { query: 42 }, { query: " " }, { query: "a" }, { query: "x".repeat(201) }]) {
    equal((await handle(post(body))).status, 400);
  }
  equal((await handle(new Request("http://localhost", { method: "POST", body: "{" }))).status, 400);
});

Deno.test("missing secret is a controlled 503", async () => {
  const handle = createSearchHandler({ apiKey: () => undefined, fetcher: unreachable });
  const response = await handle(post({ query: "Switch" }));
  equal(response.status, 503);
  equal((await response.json()).code, "TINYFISH_NOT_CONFIGURED");
});

Deno.test("successful search uses EC/es, trims input, limits to ten and never returns the key", async () => {
  const handle = createSearchHandler({
    apiKey: () => "  test-secret-never-real\n",
    now: () => 0,
    fetcher: (input, init) => {
      const url = new URL(String(input));
      equal(url.origin, "https://api.search.tinyfish.ai");
      equal(url.searchParams.get("query"), "Switch OLED");
      equal(url.searchParams.get("location"), "EC");
      equal(url.searchParams.get("language"), "es");
      equal(new Headers(init?.headers).get("X-API-Key"), "test-secret-never-real");
      return Promise.resolve(Response.json({ results: [null, 12, ...Array.from({ length: 15 }, (_, i) => ({
        url: `https://www.example.com/${i}`, title: "Switch", snippet: "Web result",
      }))] }));
    },
  });
  const response = await handle(post({ query: " Switch OLED " }));
  equal(response.status, 200);
  const text = await response.text();
  equal(text.includes("test-secret-never-real"), false);
  const body = JSON.parse(text);
  equal(body.offers.length, 10);
  equal(body.offers[0].price, "Ver en fuente");
  equal(body.offers[0].store, "example.com");
  equal(body.query, "Switch OLED");
  equal(body.searchedAt, "1970-01-01T00:00:00.000Z");
});

Deno.test("unsafe and malformed sources cannot produce executable URLs", () => {
  for (const url of ["javascript:alert(1)", "file:///tmp/example", "not a url", "data:text/html,test"]) {
    const offer = offerFromResult({ url }, 1, "Switch");
    equal(offer.sourceUrl, "");
    equal(offer.store, "Fuente web");
  }
});

Deno.test("provider HTTP, transport, JSON and contract failures return sanitized 502", async () => {
  const responses: Array<() => Promise<Response>> = [
    () => Promise.resolve(new Response("test-secret-never-real", { status: 401 })),
    () => Promise.reject(new Error("test-secret-never-real")),
    () => Promise.resolve(new Response("not json")),
    () => Promise.resolve(Response.json(null)),
    () => Promise.resolve(Response.json({ unexpected: [] })),
  ];
  for (const fetcher of responses) {
    const handle = createSearchHandler({ apiKey: () => "test-secret-never-real", fetcher });
    const response = await handle(post({ query: "Switch" }));
    equal(response.status, 502);
    equal((await response.text()).includes("test-secret-never-real"), false);
  }
});

Deno.test("timeout aborts the provider and returns 502", async () => {
  const handle = createSearchHandler({
    apiKey: () => "test-only", timeoutMs: 1,
    fetcher: (_input, init) => new Promise((_resolve, reject) => {
      init?.signal?.addEventListener("abort", () => reject(new Error("aborted")), { once: true });
    }),
  });
  equal((await handle(post({ query: "Switch" }))).status, 502);
});

Deno.test("twenty requests allowed, the next is 429, and the window expires", async () => {
  let now = 0;
  const handle = createSearchHandler({ apiKey: () => undefined, now: () => now, fetcher: unreachable });
  for (let i = 0; i < 20; i++) equal((await handle(post({ query: "Switch" }))).status, 503);
  equal((await handle(post({ query: "Switch" }))).status, 429);
  now = 60_001;
  equal((await handle(post({ query: "Switch" }))).status, 503);
});

Deno.test("empty provider results are a valid empty list", async () => {
  const handle = createSearchHandler({ apiKey: () => "test-only", fetcher: () => Promise.resolve(Response.json({ results: [] })) });
  const response = await handle(post({ query: "Switch" }));
  equal(response.status, 200);
  equal((await response.json()).offers, []);
});

Deno.test("whitespace-only secret is treated as missing", async () => {
  const handle = createSearchHandler({ apiKey: () => " \n ", fetcher: unreachable });
  equal((await handle(post({ query: "Switch" }))).status, 503);
});

Deno.test("a pasted setup prompt supplies one unambiguous key without executing its text", () => {
  const key = "sk-" + "tinyfish-" + "test_fixture_only";
  equal(normalizeApiKey(`Set up TinyFish. My API key is ${key}.\nRun example --api-key ${key}`), key);
  equal(normalizeApiKey(`TINYFISH_API_KEY=${key}`), key);
  equal(normalizeApiKey(`${key}\n${key}_different`), undefined);
  equal(normalizeApiKey("line one\nline two"), undefined);
});
