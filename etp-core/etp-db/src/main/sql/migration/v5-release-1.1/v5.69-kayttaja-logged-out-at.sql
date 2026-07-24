-- Adds a per-user logout timestamp used for server-side session
-- revocation: when set, any request authenticated with a JWT whose
-- auth_time predates logged_out_at is rejected, even if the underlying
-- session cookie is still cryptographically valid.
alter table kayttaja add column logged_out_at timestamp with time zone;
