
-- name: select-valvojat
select id, etunimi, sukunimi, rooli_id, passivoitu, valvoja
from kayttaja where rooli_id in (2, 3);
