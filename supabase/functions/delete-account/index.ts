import { accessControl } from "../_shared/access.ts";
import { createDeleteHandler } from "./delete.ts";
Deno.serve(createDeleteHandler(accessControl()));
