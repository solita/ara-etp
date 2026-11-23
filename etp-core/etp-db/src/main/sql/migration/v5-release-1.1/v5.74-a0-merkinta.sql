-- AE-2772: A0 is no longer an energy class. The boolean
-- tayttaa_a0_vaatimukset is replaced by a four-state A0 marking
-- (Ei, A0, A0_korjattu, A0_MPR) in both ET2027 and PPP.

call create_classification('a0_merkinta'::name);

-- The ids used in the data conversion are inserted immediately. Additional
-- states and contents for most columns are defined in r-a0-merkinta.sql.
insert into a0_merkinta (id) values (0), (1);

alter table energiatodistus
    add column pt$a0_merkinta_id integer references a0_merkinta (id);

alter table audit.energiatodistus
    add column pt$a0_merkinta_id integer;

-- Existing certificates converted so that the old flag maps to the
-- basic version of A0 marking (true) or to Ei (false). The flag was only
-- in use in 2027 certificates, so older versions are left empty.
-- Audit trigger is disabled so the conversion does not create new audit rows.
alter table energiatodistus disable trigger energiatodistus_update_trigger;

update energiatodistus
set pt$a0_merkinta_id = case when pt$tayttaa_a0_vaatimukset then 1 else 0 end
where versio = 2027;
update audit.energiatodistus
set pt$a0_merkinta_id = case when pt$tayttaa_a0_vaatimukset then 1 else 0 end
where versio = 2027;

alter table energiatodistus enable trigger energiatodistus_update_trigger;

alter table energiatodistus drop column pt$tayttaa_a0_vaatimukset;
alter table audit.energiatodistus drop column pt$tayttaa_a0_vaatimukset;

call audit.create_audit_procedure('energiatodistus'::name);

-- Perusparannuspassi has no audit table, so a single alter table suffices

alter table perusparannuspassi
    add column ppt$a0_merkinta_id integer references a0_merkinta (id);

update perusparannuspassi
set ppt$a0_merkinta_id = case when ppt$tayttaa_a0_vaatimukset then 1 else 0 end;

alter table perusparannuspassi drop column ppt$tayttaa_a0_vaatimukset;
