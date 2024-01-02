(ns solita.etp.schema.statistics
  (:require [schema.core :as schema]
            [schema-tools.core :as st]
            [solita.etp.schema.common :as common-schema]))

(def StatisticsQuery
  (st/optional-keys
   {:keyword schema/Str
    :kayttotarkoitus-id common-schema/Key
    :valmistumisvuosi-min common-schema/Year
    :valmistumisvuosi-max common-schema/Year
    :lammitetty-nettoala-min common-schema/NonNegative
    :lammitetty-nettoala-max common-schema/NonNegative}))

(def Versio (schema/enum 2013 2018))

(def StatisticsResponse
  (schema/maybe
    {:counts            {Versio (schema/maybe
                                  {:e-luokka {schema/Str common-schema/NonNegative}
                                   (schema/optional-key :lammitysmuoto)
                                             {common-schema/Key common-schema/NonNegative}
                                   (schema/optional-key :ilmanvaihto)
                                             {common-schema/Key common-schema/NonNegative}})}
     :e-luku-statistics {Versio (schema/maybe
                                  {:avg           common-schema/NonNegative
                                   :percentile-15 common-schema/NonNegative
                                   :percentile-85 common-schema/NonNegative})}
     :common-averages   (schema/maybe
                          (common-schema/with-maybe-vals
                            {:ilmanvuotoluku                 common-schema/NonNegative
                             :ulkoseinat-u                   common-schema/NonNegative
                             :ylapohja-u                     common-schema/NonNegative
                             :alapohja-u                     common-schema/NonNegative
                             :ikkunat-u                      common-schema/NonNegative
                             :ulkoovet-u                     common-schema/NonNegative
                             :takka                          common-schema/NonNegative
                             :ilmalampopumppu                common-schema/NonNegative
                             :tilat-ja-iv-lampokerroin       common-schema/NonNegative
                             :lammin-kayttovesi-lampokerroin common-schema/NonNegative
                             :lto-vuosihyotysuhde            common-schema/NonNegative
                             :ivjarjestelma-sfp              common-schema/NonNegative}))
     :uusiutuvat-omavaraisenergiat-counts
                        {Versio (schema/maybe {:aurinkosahko common-schema/NonNegative
                                               :aurinkolampo common-schema/NonNegative
                                               :tuulisahko   common-schema/NonNegative
                                               :lampopumppu  common-schema/NonNegative
                                               :muusahko     common-schema/NonNegative
                                               :muulampo     common-schema/NonNegative})}}))