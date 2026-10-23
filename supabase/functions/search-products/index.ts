import { createSearchHandler } from "./search.ts";
import { accessControl } from "../_shared/access.ts";

Deno.serve(createSearchHandler({
  apiKey: () => Deno.env.get("TINYFISH_API_KEY"),
  ...accessControl(),
}));
