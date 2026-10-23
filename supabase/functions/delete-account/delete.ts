type Access = {
  authorize: (request: Request) => Promise<{ id: string } | null>;
  deleteAccount: (id: string) => Promise<void>;
};
const headers = { "Content-Type": "application/json", "Access-Control-Allow-Origin": "*", "Access-Control-Allow-Headers": "authorization, apikey, content-type", "Access-Control-Allow-Methods": "DELETE, OPTIONS" };
export function createDeleteHandler(access: Access) {
  return async (request: Request) => {
    if (request.method === "OPTIONS") return new Response(null, { status: 204, headers });
    if (request.method !== "DELETE") return Response.json({ error: "Usa DELETE." }, { status: 405, headers });
    try {
      const user = await access.authorize(request);
      if (!user) return Response.json({ error: "Sesión no válida." }, { status: 401, headers });
      await access.deleteAccount(user.id);
      return new Response(null, { status: 204, headers });
    } catch {
      return Response.json({ error: "No se pudieron eliminar los datos. Inténtalo más tarde." }, { status: 503, headers });
    }
  };
}
