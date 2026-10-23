import { createDeleteHandler } from "./delete.ts";
function equal(actual: unknown, expected: unknown) {
  if (actual !== expected) throw new Error(`Expected ${expected}, got ${actual}`);
}
Deno.test("deletion requires an authenticated identity", async () => {
  let called = false;
  const handler = createDeleteHandler({ authorize: async () => null, deleteAccount: async () => { called = true; } });
  equal((await handler(new Request("https://example.test", { method: "DELETE" }))).status, 401);
  equal(called, false);
});
Deno.test("deletion uses the verified identity, never a client supplied user", async () => {
  let deleted = "";
  const handler = createDeleteHandler({ authorize: async () => ({ id: "verified-user" }), deleteAccount: async (id) => { deleted = id; } });
  const result = await handler(new Request("https://example.test?user=other", { method: "DELETE", body: JSON.stringify({ user: "other" }) }));
  equal(result.status, 204); equal(deleted, "verified-user");
});
Deno.test("deletion fails safely and methods never trigger deletion", async () => {
  const handler = createDeleteHandler({ authorize: async () => ({ id: "verified-user" }), deleteAccount: async () => { throw new Error("private detail"); } });
  equal((await handler(new Request("https://example.test", { method: "OPTIONS" }))).status, 204);
  equal((await handler(new Request("https://example.test"))).status, 405);
  const result = await handler(new Request("https://example.test", { method: "DELETE" }));
  equal(result.status, 503); equal((await result.text()).includes("private detail"), false);
});
