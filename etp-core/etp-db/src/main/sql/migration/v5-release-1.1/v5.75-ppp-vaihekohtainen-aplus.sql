-- AE-2913: Move the PPP A+ requirement flag from pass-level data to each phase.

alter table perusparannuspassi_vaihe
    add column tayttaa_aplus_vaatimukset boolean not null default false;

-- Preserve the old final-state value on the last active phase only.
update perusparannuspassi_vaihe vaihe
set tayttaa_aplus_vaatimukset = true
from perusparannuspassi ppp
where vaihe.perusparannuspassi_id = ppp.id
  and ppp.ppt$tayttaa_aplus_vaatimukset = true
  and vaihe.valid = true
  and vaihe.t$vaiheen_alku_pvm is not null
  and vaihe.vaihe_nro = (
    select max(viimeinen.vaihe_nro)
    from perusparannuspassi_vaihe viimeinen
    where viimeinen.perusparannuspassi_id = vaihe.perusparannuspassi_id
      and viimeinen.valid = true
      and viimeinen.t$vaiheen_alku_pvm is not null
  );

alter table perusparannuspassi
    drop column ppt$tayttaa_aplus_vaatimukset;

