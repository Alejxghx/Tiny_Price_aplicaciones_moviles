-- Contadores sin consultas ni claves. Solo el servidor puede acceder a ellos.
create schema if not exists private;
revoke all on schema private from public, anon, authenticated;
grant usage on schema private to service_role;

create table private.tinyprice_limits (
  id boolean primary key default true check (id),
  enabled boolean not null default true,
  user_per_minute integer not null default 10 check (user_per_minute > 0),
  user_per_day integer not null default 50 check (user_per_day > 0),
  global_per_day integer not null default 500 check (global_per_day > 0)
);
insert into private.tinyprice_limits (id) values (true);
alter table private.tinyprice_limits enable row level security;

create table private.tinyprice_usage (
  bucket text not null,
  period_start timestamptz not null,
  used integer not null default 0 check (used >= 0),
  primary key (bucket, period_start)
);
alter table private.tinyprice_usage enable row level security;
revoke all on private.tinyprice_limits, private.tinyprice_usage from public, anon, authenticated;
grant select, update on private.tinyprice_limits to service_role;
grant select, insert, update, delete on private.tinyprice_usage to service_role;

create function public.reserve_search_quota(p_user_id uuid)
returns jsonb
language plpgsql
security invoker
set search_path = ''
as $$
declare
  config private.tinyprice_limits%rowtype;
  current_time timestamptz := now();
  minute_start timestamptz := date_trunc('minute', current_time);
  day_start timestamptz := date_trunc('day', current_time at time zone 'UTC') at time zone 'UTC';
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
    return jsonb_build_object('allowed', false, 'retryAfter', ceil(extract(epoch from day_start + interval '1 day' - current_time))::integer, 'reason', 'daily');
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
revoke all on function public.reserve_search_quota(uuid) from public, anon, authenticated;
grant execute on function public.reserve_search_quota(uuid) to service_role;

create function public.delete_search_usage(p_user_id uuid)
returns void language sql security invoker set search_path = '' as $$
  delete from private.tinyprice_usage where bucket in ('minute:' || p_user_id::text, 'day:' || p_user_id::text);
$$;
revoke all on function public.delete_search_usage(uuid) from public, anon, authenticated;
grant execute on function public.delete_search_usage(uuid) to service_role;

-- El event trigger funciona como propietario; los clientes no deben ejecutarlo.
revoke all on function public.rls_auto_enable() from public, anon, authenticated;
