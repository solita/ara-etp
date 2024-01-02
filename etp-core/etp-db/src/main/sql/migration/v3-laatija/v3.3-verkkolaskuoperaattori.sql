CREATE TABLE verkkolaskuoperaattori (
  id int PRIMARY KEY,
  valittajatunnus text unique,
  nimi text NOT NULL,
  valid boolean NOT NULL DEFAULT true
);
call audit.activate('verkkolaskuoperaattori'::name);