import { createSearchHandler } from "./search.ts";

Deno.serve(createSearchHandler({
  apiKey: () => Deno.env.get("TINYFISH_API_KEY"),
}));
