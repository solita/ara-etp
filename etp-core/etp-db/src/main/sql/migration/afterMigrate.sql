
grant select on all tables in schema etp, audit to etp_app;
grant execute on all functions in schema etp to etp_app;
grant usage on schema etp to etp_app;
grant usage on schema audit to etp_app;

grant insert, update on table kayttaja to etp_app;
grant insert, update on table laatija to etp_app;
grant insert, update on table energiatodistus to etp_app;
grant insert, update on table yritys to etp_app;
grant insert, update on table laatija_yritys to etp_app;
grant insert, update on table liite to etp_app;
grant insert, update on table viestiketju to etp_app;
grant insert, update on table viesti to etp_app;
grant insert, update on table vastaanottaja to etp_app;
grant insert on table viesti_reader to etp_app;
grant insert, update on table viesti_liite to etp_app;
grant insert, update, delete on table sivu to etp_app;
grant insert, update, delete on table kayttaja_aineisto to etp_app;

grant insert, update on table vo_toimenpide to etp_app;
grant delete, insert, update on table vo_virhe to etp_app;
grant insert, update on table vo_note to etp_app;
grant delete, insert on table vo_tiedoksi to etp_app;
grant insert, update on table vo_virhetype to etp_app;

grant insert, update on table vk_valvonta to etp_app;
grant insert, update on table vk_toimenpide to etp_app;
grant insert, update on table vk_henkilo to etp_app;
grant insert, update on table vk_yritys to etp_app;
grant insert, update on table vk_valvonta_liite to etp_app;
grant insert on table vk_toimenpide_henkilo to etp_app;
grant insert on table vk_toimenpide_yritys to etp_app;
grant insert, update on table vk_note to etp_app;

grant delete on table audit.energiatodistus to etp_app;
grant delete on table vo_toimenpide to etp_app;
grant delete on table audit.vo_toimenpide to etp_app;
grant delete on table vo_note to etp_app;
grant delete on table audit.vo_note to etp_app;
grant delete on table vo_virhe to etp_app;
-- no audit for vo_virhe
grant delete on table vo_tiedoksi to etp_app;
-- no audit for vo_tiedoksi
grant delete on table liite to etp_app;
grant delete on table audit.liite to etp_app;

grant delete on table viestiketju to etp_app;
grant delete on table audit.viestiketju to etp_app;
grant delete on table viesti_liite to etp_app;
grant delete on table audit.viesti_liite to etp_app;
grant delete on table viesti to etp_app;
-- no audit for viesti;
grant delete on table vastaanottaja to etp_app;
-- no audit for vastaanottaja;
grant delete on table viesti_reader to etp_app;
-- no audit for viesti_reader;
