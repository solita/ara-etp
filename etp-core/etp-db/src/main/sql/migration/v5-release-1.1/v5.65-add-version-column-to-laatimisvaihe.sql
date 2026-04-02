-- Step 1: Add versio column to laatimisvaihe
alter table laatimisvaihe add column if not exists versio int not null default 2018;

-- Step 2: Add versio column to audit table
alter table audit.laatimisvaihe add column if not exists versio int;

-- Step 3: Drop old FK from energiatodistus
alter table energiatodistus drop constraint if exists energiatodistus_pt$laatimisvaihe_fkey;

-- Step 4: Drop old PK and create composite PK
alter table laatimisvaihe drop constraint if exists laatimisvaihe_pkey;
alter table laatimisvaihe add primary key (id, versio);

-- Step 5: Create composite FK tying energiatodistus (pt$laatimisvaihe, versio) to laatimisvaihe (id, versio).
-- When pt$laatimisvaihe is NULL (drafts or 2013 version), the FK is not enforced.
alter table energiatodistus
  add foreign key (pt$laatimisvaihe, versio) references laatimisvaihe (id, versio);

-- Step 6: Refresh audit procedure to include the new versio column
call audit.create_audit_procedure('laatimisvaihe'::name);
