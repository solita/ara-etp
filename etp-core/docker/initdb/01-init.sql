create user etp with createdb password 'etp';
create user etp_app with password 'etp';

alter database postgres is_template true;

grant all privileges on database postgres to etp;

create database etp_dev template postgres;
grant all privileges on database etp_dev to etp;
