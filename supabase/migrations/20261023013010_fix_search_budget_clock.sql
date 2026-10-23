create or replace function public.reserve_search_quota(p_user_id uuid)
returns jsonb
language plpgsql
security invoker
set search_path = ''
as $$
declare
  config private.tinyprice_limits%rowtype;
  budget_now timestamptz := now();
  minute_start timestamptz := date_trunc('minute', budget_now);
  day_start timestamptz := date_trunc('day', budget_now at time zone 'UTC') at time zone 'UTC';
  minute_bucket text := 'minute:' || p_user_id::text;
  day_bucket text := 'day:' || p_user_id::text;
  global_used integer;
  day_used integer;
  minute_used integer;
begin
  if p_user_id is null then raise exception 'Missing user'; end if;
  -- Serializa la reserva para impedir superar el tope con solicitudes concurrentes.
  perform pg_catalog.pg_advisory_xact_lock(737184021);
  select * into strict config from private.tinyprice_limits where id = true;
  if not config.enabled then
    return jsonb_build_object('allowed', false, 'retryAfter', 3600, 'reason', 'paused');
  end if;
  delete from private.tinyprice_usage where period_start < day_start - interval '7 days';
  select coalesce(max(used), 0) into minute_used from private.tinyprice_usage where bucket = minute_bucket and period_start = minute_start;
  select coalesce(max(used), 0) into day_used from private.tinyprice_usage where bucket = day_bucket and period_start = day_start;
  select coalesce(max(used), 0) into global_used from private.tinyprice_usage where bucket = 'global' and period_start = day_start;
  if global_used >= config.global_per_day or day_used >= config.user_per_day then
    return jsonb_build_object('allowed', false, 'retryAfter', ceil(extract(epoch from day_start + interval '1 day' - budget_now))::integer, 'reason', 'daily');
  end if;
  if minute_used >= config.user_per_minute then
    return jsonb_build_object('allowed', false, 'retryAfter', 60, 'reason', 'minute');
  end if;
  insert into private.tinyprice_usage (bucket, period_start, used)
    values (minute_bucket, minute_start, 1), (day_bucket, day_start, 1), ('global', day_start, 1)
    on conflict (bucket, period_start) do update set used = private.tinyprice_usage.used + 1;
  return jsonb_build_object('allowed', true, 'remainingToday', config.user_per_day - day_used - 1);
end;
$$;

create policy tinyprice_limits_server_only on private.tinyprice_limits for all to service_role using (true) with check (true);
create policy tinyprice_usage_server_only on private.tinyprice_usage for all to service_role using (true) with check (true);
notify pgrst, 'reload schema';
