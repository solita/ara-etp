alter table energiatodistus
    add column pt$tayttaa_aplus_vaatimukset boolean not null default false,
    add column pt$tayttaa_a0_vaatimukset boolean not null default false,
    alter column t$e_luokka type varchar(2);

alter table audit.energiatodistus
    alter column t$e_luokka type varchar(2);
