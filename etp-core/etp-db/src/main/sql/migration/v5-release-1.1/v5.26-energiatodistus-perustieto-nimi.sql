alter table energiatodistus add column pt$nimi_fi text;
alter table energiatodistus add column pt$nimi_sv text;

alter table audit.energiatodistus
  add column pt$nimi_fi text;

alter table audit.energiatodistus
  add column pt$nimi_sv text;

call audit.create_audit_procedure('energiatodistus'::name);

update energiatodistus
set pt$nimi_fi=pt$nimi
where pt$kieli=0;

update energiatodistus
set pt$nimi_sv=pt$nimi
where pt$kieli=1;

update energiatodistus
set pt$nimi_fi=pt$nimi,
    pt$nimi_sv=pt$nimi
where pt$kieli=2 or pt$kieli is null;