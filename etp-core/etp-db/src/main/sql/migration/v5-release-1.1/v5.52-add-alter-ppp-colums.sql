alter table perusparannuspassi
    rename column t$lisatiedot_fi to t$lisatietoja_fi;

alter table perusparannuspassi
    rename column t$lisatiedot_sv to t$lisatietoja_sv;

alter table perusparannuspassi
    add column ppt$lisatietoja_fi text,
    add column ppt$lisatietoja_sv text,
    add column rpt$lisatietoja_fi text,
    add column rpt$lisatietoja_sv text,
    add column rpt$ulkoovet_ehdotettu_taso numeric;
