-- Remove the unique constraint on energiatodistus_id
-- and replace it with a partial unique index that only applies to valid rows
alter table perusparannuspassi drop constraint perusparannuspassi_energiatodistus_id_key;

-- Create a partial unique index that only enforces uniqueness for valid PPPs
create unique index perusparannuspassi_energiatodistus_id_valid_key
  on perusparannuspassi(energiatodistus_id)
  where valid = true;
