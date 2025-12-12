call create_classification('toimenpide_ehdotus_group'::name);

alter table toimenpide_ehdotus
    add column group_id integer references toimenpide_ehdotus_group(id);
