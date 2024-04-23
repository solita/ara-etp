CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

alter table laatija
    add column allekirjoitus_id uuid default uuid_generate_v4() not null;
