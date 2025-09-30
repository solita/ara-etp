(ns solita.etp.schema.perusparannuspassi
  (:require
    [schema.core :as schema]
    [solita.common.schema :as xschema]
    [solita.etp.schema.common :as common-schema]))

(def PassiPerustiedot
  {:havainnointikaynti        common-schema/Date
   :passin-esittely           common-schema/Date
   :tayttaa-aplus-vaatimukset schema/Bool
   :tayttaa-a0-vaatimukset    schema/Bool})

(def ToimenpideEhdotus
  {:id          common-schema/Key
   :label_fi    common-schema/String150
   :label_sv    common-schema/String150
   :valid       schema/Bool
   :ordinal     schema/Num
   :description common-schema/String150})

(def Rakennuksenperustiedot
  {:ulkoseinat-ehdotettu-taso                schema/Num
   :ylapohja-ehdotettu-taso                  schema/Num
   :alapohja-ehdotettu-taso                  schema/Num
   :ikkunat-ehdotettu-taso                   schema/Num
   :paalammitysjarjestelma-ehdotettu-taso    common-schema/Key
   :ilmanvaihto-ehdotettu-taso               common-schema/Key
   :uusiutuva-energia-ehdotettu-taso         common-schema/Key
   :jaahdytys-ehdotettu-taso                 common-schema/Key
   :mahdollisuus-liittya-energiatehokkaaseen common-schema/Key})

(def LaskennanTulokset
  {:kaukolampo-hinta      schema/Num
   :sahko-hinta           schema/Num
   :uusiutuvat-pat-hinta  schema/Num
   :fossiiliset-pat-hinta schema/Num
   :kaukojaahdytys-hinta  schema/Num
   :lisatiedot            common-schema/String1500})

(def VaiheLaskennanTulokset
  {:vaiheen-alku-pvm                      common-schema/Date
   :vaiheen-loppu-pvm                     common-schema/Date
   :ostoenergian-tarve-kaukolampo         schema/Num
   :ostoenergian-tarve-sahko              schema/Num
   :ostoenergian-tarve-uusiutuvat-pat     schema/Num
   :ostoenergian-tarve-fossiiliset-pat    schema/Num
   :ostoenergian-tarve-kaukojaahdytys     schema/Num
   :uusiutuvan-energian-kokonaistuotto    schema/Num
   :toteutunut-ostoenergia-kaukolampo     schema/Num
   :toteutunut-ostoenergia-sahko          schema/Num
   :toteutunut-ostoenergia-uusiutuvat-pat schema/Num
   :toteutunut-ostoenegia-fossiiliset-pat schema/Num
   :toteutunut-ostoenergia-kaukojaahdytys schema/Num})

(def Toimenpiteet
  {:toimenpideseloste     common-schema/String150
   :toimenpide-ehdotukset [ToimenpideEhdotus]})

(def PerusparannuspassiVaihe
  {:vaihe-nro         (common-schema/LimitedInt 1 4)
   :valid             schema/Bool
   :toimenpiteet      (xschema/optional-properties Toimenpiteet)
   :laskennantulokset (xschema/optional-properties VaiheLaskennanTulokset)})

(def PerusparannuspassiSave
  {:valid                  schema/Bool
   :energiatodistus-id     common-schema/Key
   :perustiedot            (xschema/optional-properties PassiPerustiedot)
   :vaiheet                [PerusparannuspassiVaihe]
   :rakennuksenperustiedot (xschema/optional-properties Rakennuksenperustiedot)
   :laskennanperustiedot   (xschema/optional-properties LaskennanTulokset)})

(def Perusparannuspassi
  (merge common-schema/Id
         {:laatija-id common-schema/Key
          :tila-id    common-schema/Key}
         PerusparannuspassiSave))
