
alter table validation_required_column add column bypass_allowed boolean default false;
alter table audit.validation_required_column add column bypass_allowed boolean default false;
call audit.create_audit_procedure('validation_required_column'::name);