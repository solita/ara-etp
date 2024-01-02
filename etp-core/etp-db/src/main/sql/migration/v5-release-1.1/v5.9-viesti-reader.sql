
create table viesti_reader (
  viesti_id int not null references viesti (id),
  reader_id int not null default etp.current_kayttaja_id() references kayttaja (id),
  read_time timestamp with time zone not null default transaction_timestamp(),

  primary key (viesti_id, reader_id) include (read_time)
);