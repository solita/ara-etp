
alter table yritys drop constraint yritys_ytunnus_nimi_vastaanottajan_tarkenne_key;

create unique index yritys_ytunnus_nimi_vastaanottajan_tarkenne_key
  on yritys (ytunnus, nimi, vastaanottajan_tarkenne) where (not deleted);
