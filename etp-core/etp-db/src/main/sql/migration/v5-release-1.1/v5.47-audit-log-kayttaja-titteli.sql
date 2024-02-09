alter table audit.kayttaja add column titteli_fi text default null;
alter table audit.kayttaja add column titteli_sv text default null;

drop trigger if exists kayttaja_update_trigger on kayttaja;
call audit.create_audit_procedure('kayttaja'::name);
call audit.create_audit_update_trigger('kayttaja'::name, 'kayttaja'::name,
                                       audit.update_condition('kayttaja'::name));