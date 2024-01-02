
create unique index if not exists yritys_ytunnus_nimi_key on etp.yritys (ytunnus, nimi)
  where (not deleted and vastaanottajan_tarkenne is null);
