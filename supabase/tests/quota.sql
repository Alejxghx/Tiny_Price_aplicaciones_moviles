begin;
set local role service_role;
do $$
declare
  test_user uuid := gen_random_uuid();
  another_user uuid := gen_random_uuid();
  result jsonb;
  baseline integer;
begin
  select coalesce(max(used), 0) into baseline from private.tinyprice_usage where bucket = 'global' and period_start = date_trunc('day', now() at time zone 'UTC') at time zone 'UTC';
  update private.tinyprice_limits set enabled = true, user_per_minute = 2, user_per_day = 50, global_per_day = baseline + 100;
  result := public.reserve_search_quota(test_user);
  assert (result->>'allowed')::boolean;
  result := public.reserve_search_quota(test_user);
  assert (result->>'allowed')::boolean;
  result := public.reserve_search_quota(test_user);
  assert not (result->>'allowed')::boolean and result->>'reason' = 'minute';
  update private.tinyprice_limits set user_per_minute = 100, user_per_day = 2;
  result := public.reserve_search_quota(test_user);
  assert not (result->>'allowed')::boolean and result->>'reason' = 'daily';
  update private.tinyprice_limits set user_per_day = 50, global_per_day = baseline + 2;
  result := public.reserve_search_quota(another_user);
  assert not (result->>'allowed')::boolean;
  update private.tinyprice_limits set enabled = false;
  result := public.reserve_search_quota(another_user);
  assert not (result->>'allowed')::boolean and result->>'reason' = 'paused';
  perform public.delete_search_usage(test_user);
  assert not exists (select 1 from private.tinyprice_usage where bucket in ('minute:' || test_user::text, 'day:' || test_user::text));
  assert (select used = baseline + 2 from private.tinyprice_usage where bucket = 'global' and period_start = date_trunc('day', now() at time zone 'UTC') at time zone 'UTC');
end $$;
rollback;
select not has_function_privilege('anon', 'public.reserve_search_quota(uuid)', 'EXECUTE') as anon_blocked,
       not has_function_privilege('authenticated', 'public.reserve_search_quota(uuid)', 'EXECUTE') as client_blocked,
       not has_function_privilege('anon', 'public.rls_auto_enable()', 'EXECUTE') as trigger_blocked;
