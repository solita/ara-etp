-- name: kaskypaatos-kuulemiskirje
with kehotus as (select create_time, deadline_date, valvonta_id
                 from vk_toimenpide
                 where valvonta_id = :valvonta-id
                   and type_id = 2
                 order by create_time desc
                 LIMIT 1),
     varoitus as (select deadline_date, valvonta_id
                  from vk_toimenpide
                  where valvonta_id = :valvonta-id
                    and type_id = 3
                  order by create_time desc
                  LIMIT 1)
select kehotus.create_time::date as kehotus_pvm,
       kehotus.deadline_date     as kehotus_maarapaiva,
       varoitus.deadline_date    as varoitus_maarapaiva
from kehotus
         left join varoitus on kehotus.valvonta_id = varoitus.valvonta_id;

-- name: kaskypaatos-varsinainen-paatos
with kehotus as (select deadline_date, valvonta_id
                 from vk_toimenpide
                 where valvonta_id = :valvonta-id
                   and type_id = 2
                 order by create_time desc
                 LIMIT 1),
     varoitus as (select deadline_date, valvonta_id
                  from vk_toimenpide
                  where valvonta_id = :valvonta-id
                    and type_id = 3
                  order by create_time desc
                  LIMIT 1),
     kuulemiskirje as (select create_time,
                              valvonta_id,
                              diaarinumero,
                              type_specific_data -> 'fine' as kuulemiskirje_fine
                       from vk_toimenpide
                       where valvonta_id = :valvonta-id
                         and type_id = 7
                       order by create_time desc
                       LIMIT 1)
select kehotus.deadline_date            as kehotus_maarapaiva,
       varoitus.deadline_date           as varoitus_maarapaiva,
       kuulemiskirje.create_time::date  as kuulemiskirje_pvm,
       kuulemiskirje.diaarinumero       as kuulemiskirje_diaarinumero,
       kuulemiskirje.kuulemiskirje_fine as kuulemiskirje_fine
from kehotus
         left join varoitus on kehotus.valvonta_id = varoitus.valvonta_id
         left join kuulemiskirje on kehotus.valvonta_id = kuulemiskirje.valvonta_id;

-- name: sakkopaatos-kuulemiskirje
select create_time::date as varsinainen_paatos_pvm,
       deadline_date varsinainen_paatos_maarapaiva
from vk_toimenpide
where valvonta_id = :valvonta-id
  and type_id = 8
order by create_time desc
LIMIT 1;

-- name: sakkopaatos-varsinainen-paatos
with varsinainen_paatos as (select create_time,
                                   deadline_date,
                                   valvonta_id,
                                   type_specific_data -> 'fine' as varsinainen_paatos_fine,
                                   diaarinumero
                            from vk_toimenpide
                            where valvonta_id = :valvonta-id
                              and type_id = 8
                            order by create_time desc
                            LIMIT 1),
     sakkopaatos_kuulemiskirje as (select create_time, diaarinumero, valvonta_id
                                   from vk_toimenpide
                                   where valvonta_id = :valvonta-id
                                     and type_id = 14
                                   order by create_time desc
                                   LIMIT 1)
select varsinainen_paatos.create_time::date        as varsinainen_paatos_pvm,
       varsinainen_paatos.deadline_date            as varsinainen_paatos_maarapaiva,
       varsinainen_paatos.varsinainen_paatos_fine  as varsinainen_paatos_fine,
       varsinainen_paatos.diaarinumero             as varsinainen_paatos_diaarinumero,
       sakkopaatos_kuulemiskirje.create_time::date as sakkopaatos_kuulemiskirje_pvm,
       sakkopaatos_kuulemiskirje.diaarinumero      as sakkopaatos_kuulemiskirje_diaarinumero
from varsinainen_paatos
         left join sakkopaatos_kuulemiskirje on varsinainen_paatos.valvonta_id = sakkopaatos_kuulemiskirje.valvonta_id;
