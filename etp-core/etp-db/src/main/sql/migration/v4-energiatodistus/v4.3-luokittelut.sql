call create_classification('kielisyys'::name);
call create_classification('laatimisvaihe'::name);
call create_classification('ilmanvaihtotyyppi'::name);
call create_classification('lammitysmuoto'::name);
call create_classification('lammonjako'::name);

call audit.activate('kielisyys'::name);
call audit.activate('laatimisvaihe'::name);
call audit.activate('ilmanvaihtotyyppi'::name);
call audit.activate('lammitysmuoto'::name);
call audit.activate('lammonjako'::name);