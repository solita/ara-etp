-- Add the organisation name for aineistokäyttäjät
alter table kayttaja add column organisaatio text not null default '';
alter table audit.kayttaja add column organisaatio text;

drop trigger if exists kayttaja_update_trigger on kayttaja;
call audit.create_audit_procedure('kayttaja'::name);
call audit.create_audit_update_trigger('kayttaja'::name, 'kayttaja'::name,
  audit.update_condition('kayttaja'::name));

-- Define classification for different kinds of aineisto that can be offered
call create_classification('aineisto'::name);
call audit.activate('aineisto'::name);

create table kayttaja_aineisto (
  kayttaja_id int not null references kayttaja(id),
  aineisto_id int not null references aineisto(id),
  valid_until timestamp with time zone not null,
  ip_address text not null,
  unique(kayttaja_id, aineisto_id)
);
