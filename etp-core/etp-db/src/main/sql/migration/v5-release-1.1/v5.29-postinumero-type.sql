call create_classification('postinumerotype'::name);
call audit.activate('postinumerotype'::name);

alter table postinumero add column type_id int references postinumerotype(id);
