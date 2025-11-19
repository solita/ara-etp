create table ppp_validation_numeric_column (
    column_name text,
    versio int,
    warning$min numeric,
    warning$max numeric,
    error$min numeric,
    error$max numeric,
    valid boolean default true,
    primary key (column_name, versio)
);

create table ppp_validation_required_column (
    column_name text,
    versio int,
    ordinal int not null default 0,
    valid boolean default true,
    bypass_allowed boolean default false,
    primary key (column_name, versio)
);

create table ppp_vaihe_validation_numeric_column (
    column_name text,
    versio int,
    warning$min numeric,
    warning$max numeric,
    error$min numeric,
    error$max numeric,
    valid boolean default true,
    primary key (column_name, versio)
);

create table ppp_vaihe_validation_required_column (
    column_name text,
    versio int,
    ordinal int not null default 0,
    valid boolean default true,
    bypass_allowed boolean default false,
    primary key (column_name, versio)
);
