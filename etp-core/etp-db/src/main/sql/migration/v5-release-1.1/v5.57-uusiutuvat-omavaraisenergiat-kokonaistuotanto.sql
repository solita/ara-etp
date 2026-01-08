alter table energiatodistus
    add column t$uusiutuvat_omavaraisenergiat$aurinkosahko_kokonaistuotanto numeric,
    add column t$uusiutuvat_omavaraisenergiat$tuulisahko_kokonaistuotanto numeric,
    add column t$uusiutuvat_omavaraisenergiat$lampopumppu_kokonaistuotanto numeric,
    add column t$uusiutuvat_omavaraisenergiat$muulampo_kokonaistuotanto numeric,
    add column t$uusiutuvat_omavaraisenergiat$muusahko_kokonaistuotanto numeric;
