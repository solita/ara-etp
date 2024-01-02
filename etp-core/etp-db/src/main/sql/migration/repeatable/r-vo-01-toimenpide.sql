
create or replace function etp.vo_toimenpide_visible_laatija(toimenpide vo_toimenpide) returns boolean as $$
begin
  return (toimenpide.type_id in (1, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13) and
           toimenpide.publish_time is not null) or
         toimenpide.type_id in (4, 8, 12);
end;
$$ language plpgsql immutable;

create or replace function etp.vo_toimenpide_ongoing(last_toimenpide vo_toimenpide) returns boolean as $$
begin
  return last_toimenpide.type_id not in (0, 1, 14);
end;
$$ language plpgsql immutable;