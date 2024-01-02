ALTER TABLE viestiketju ADD COLUMN kasitelty boolean NOT NULL DEFAULT false;
ALTER TABLE viestiketju ADD COLUMN kasittelija_id int REFERENCES kayttaja (id);
CREATE INDEX viestiketju_kasittelija_id_idx ON viestiketju (kasittelija_id);
CALL audit.activate('viestiketju'::name);
