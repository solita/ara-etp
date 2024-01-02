
create or replace view etp.vo_last_toimenpide_v1 as
select distinct on (toimenpide.energiatodistus_id)
  toimenpide.id, 
  toimenpide.type_id, 
  toimenpide.author_id,
  toimenpide.energiatodistus_id,
  toimenpide.create_time,
  toimenpide.publish_time,
  toimenpide.deadline_date,
  toimenpide.template_id,
  toimenpide.diaarinumero,
  toimenpide.description,
  toimenpide.severity_id,
  etp.vo_toimenpide_ongoing(toimenpide) ongoing,
  etp.vo_toimenpide_visible_laatija(toimenpide) visible_laatija,
  toimenpide.type_id in (3, 5, 6, 7, 9, 10, 11) or
    (toimenpide.type_id in (4, 8, 12) and toimenpide.publish_time is null) unfinished_laatija,
  lead(toimenpide.deadline_date) over (
    partition by toimenpide.energiatodistus_id
    order by toimenpide.create_time desc, toimenpide.id desc) previous_deadline_date
from etp.vo_toimenpide toimenpide
where toimenpide.deleted is false
order by toimenpide.energiatodistus_id, toimenpide.create_time desc, toimenpide.id desc;
