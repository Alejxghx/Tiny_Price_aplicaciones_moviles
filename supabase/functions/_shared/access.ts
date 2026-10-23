function envKey(name: string, legacy: string): string {
  const values = Deno.env.get(name);
  return (values ? JSON.parse(values).default : undefined) ?? Deno.env.get(legacy) ?? "";
}

export function accessControl() {
  const baseUrl = Deno.env.get("SUPABASE_URL")!;
  const publishableKey = envKey("SUPABASE_PUBLISHABLE_KEYS", "SUPABASE_ANON_KEY");
  const secretKey = envKey("SUPABASE_SECRET_KEYS", "SUPABASE_SERVICE_ROLE_KEY");
  const signal = () => AbortSignal.timeout(5_000);
  const adminHeaders = { apikey: secretKey, "Content-Type": "application/json" };
  return {
    async authorize(request: Request): Promise<{ id: string } | null> {
      const authorization = request.headers.get("authorization");
      if (!authorization?.startsWith("Bearer ")) return null;
      const response = await fetch(`${baseUrl}/auth/v1/user`, {
        headers: { apikey: publishableKey, Authorization: authorization }, signal: signal(),
      });
      if (response.status === 401 || response.status === 403) { await response.body?.cancel(); return null; }
      if (!response.ok) { await response.body?.cancel(); throw new Error("Auth unavailable"); }
      const user = await response.json();
      return typeof user.id === "string" ? { id: user.id } : null;
    },
    async reserveQuota(userId: string): Promise<{ allowed: boolean; retryAfter?: number }> {
      const response = await fetch(`${baseUrl}/rest/v1/rpc/reserve_search_quota`, {
        method: "POST", headers: adminHeaders, body: JSON.stringify({ p_user_id: userId }), signal: signal(),
      });
      if (!response.ok) { await response.body?.cancel(); throw new Error("Quota unavailable"); }
      const result = await response.json();
      if (typeof result?.allowed !== "boolean") throw new Error("Invalid quota response");
      return result;
    },
    async deleteAccount(userId: string) {
      const cleanup = await fetch(`${baseUrl}/rest/v1/rpc/delete_search_usage`, {
        method: "POST", headers: adminHeaders, body: JSON.stringify({ p_user_id: userId }), signal: signal(),
      });
      if (!cleanup.ok) { await cleanup.body?.cancel(); throw new Error("Deletion unavailable"); }
      await cleanup.body?.cancel();
      const response = await fetch(`${baseUrl}/auth/v1/admin/users/${encodeURIComponent(userId)}`, {
        method: "DELETE", headers: { apikey: secretKey, Authorization: `Bearer ${secretKey}` }, signal: signal(),
      });
      if (!response.ok) { await response.body?.cancel(); throw new Error("Deletion unavailable"); }
      await response.body?.cancel();
    },
  };
}
