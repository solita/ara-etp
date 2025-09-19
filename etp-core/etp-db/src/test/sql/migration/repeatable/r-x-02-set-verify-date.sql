set session_replication_role = 'replica';
-- See e2e test tiedot-tarkastamatta.cy.js
update kayttaja set verifytime = now() where kayttaja.email != 'tiedottarkastamatta@example.com';
