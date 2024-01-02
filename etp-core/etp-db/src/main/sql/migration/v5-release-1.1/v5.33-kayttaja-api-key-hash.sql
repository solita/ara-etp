alter table kayttaja add column api_key_hash text;

update kayttaja
set api_key_hash = laatija.api_key_hash
from laatija
where kayttaja.id = laatija.id;

alter table audit.kayttaja add column api_key_hash text;

drop trigger if exists kayttaja_update_trigger on kayttaja;
call audit.create_audit_procedure('kayttaja'::name);
call audit.create_audit_update_trigger('kayttaja'::name, 'kayttaja'::name,
  audit.update_condition('kayttaja'::name));
