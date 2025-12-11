(ns solita.etp.schema.perusparannuspassi
  (:require
    [schema.core :as schema]
    [solita.common.schema :as xschema]
    [solita.etp.schema.common :as common-schema]))

(def PassiPerustiedot
  {:havainnointikaynti        common-schema/Date
   :passin-esittely           common-schema/Date
   :tayttaa-aplus-vaatimukset schema/Bool
   :tayttaa-a0-vaatimukset    schema/Bool
   :lisatietoja-fi            common-schema/String1500
   :lisatietoja-sv            common-schema/String1500})

(def Rakennuksenperustiedot
  {:ulkoseinat-ehdotettu-taso                schema/Num
   :ylapohja-ehdotettu-taso                  schema/Num
   :alapohja-ehdotettu-taso                  schema/Num
   :ikkunat-ehdotettu-taso                   schema/Num
   :ulkoovet-ehdotettu-taso                  schema/Num
   :paalammitysjarjestelma-ehdotettu-taso    common-schema/Key
   :ilmanvaihto-ehdotettu-taso               common-schema/Key
   :uusiutuva-energia-ehdotettu-taso         common-schema/Key
   :jaahdytys-ehdotettu-taso                 common-schema/Key
   :mahdollisuus-liittya-energiatehokkaaseen common-schema/Key
   :lisatietoja-fi         common-schema/String1500
   :lisatietoja-sv         common-schema/String1500})

(def LaskennanTulokset
  {:kaukolampo-hinta      schema/Num
   :sahko-hinta           schema/Num
   :uusiutuvat-pat-hinta  schema/Num
   :fossiiliset-pat-hinta schema/Num
   :kaukojaahdytys-hinta  schema/Num
   :lisatietoja-fi        common-schema/String1500
   :lisatietoja-sv        common-schema/String1500})

(def VaiheLaskennanTulokset
  {:vaiheen-alku-pvm                       common-schema/Date
   :vaiheen-loppu-pvm                      common-schema/Date
   :ostoenergian-tarve-kaukolampo          schema/Num
   :ostoenergian-tarve-sahko               schema/Num
   :ostoenergian-tarve-uusiutuvat-pat      schema/Num
   :ostoenergian-tarve-fossiiliset-pat     schema/Num
   :ostoenergian-tarve-kaukojaahdytys      schema/Num
   :uusiutuvan-energian-kokonaistuotto     schema/Num
   :uusiutuvan-energian-hyodynnetty-osuus  schema/Num
   :toteutunut-ostoenergia-kaukolampo      schema/Num
   :toteutunut-ostoenergia-sahko           schema/Num
   :toteutunut-ostoenergia-uusiutuvat-pat  schema/Num
   :toteutunut-ostoenergia-fossiiliset-pat schema/Num
   :toteutunut-ostoenergia-kaukojaahdytys  schema/Num})

(def Toimenpiteet
  {:toimenpideseloste-fi  (schema/maybe common-schema/String1500)
   :toimenpideseloste-sv  (schema/maybe common-schema/String1500)
   :toimenpide-ehdotukset [common-schema/Id]})

(def PerusparannuspassiVaihe
  {:vaihe-nro    (common-schema/LimitedInt 1 4)
   :valid        schema/Bool
   :toimenpiteet Toimenpiteet
   :tulokset (xschema/optional-properties VaiheLaskennanTulokset)})

(def PerusparannuspassiSave
  {:valid                  schema/Bool
   :energiatodistus-id     common-schema/Key
   :passin-perustiedot            (xschema/optional-properties PassiPerustiedot)
   :vaiheet                [PerusparannuspassiVaihe]
   :rakennuksen-perustiedot (xschema/optional-properties Rakennuksenperustiedot)
   :tulokset   (xschema/optional-properties LaskennanTulokset)})

(def Perusparannuspassi
  (-> PerusparannuspassiSave
      (merge common-schema/Id
             {:laatija-id common-schema/Key
              :tila-id    common-schema/Key})

      (update-in [:vaiheet 0 :toimenpiteet :toimenpide-ehdotukset 0]
                 (fn [toimenpide-ehdotus-save-schema]
                   (merge toimenpide-ehdotus-save-schema
                          {:group-id common-schema/Key})))))
