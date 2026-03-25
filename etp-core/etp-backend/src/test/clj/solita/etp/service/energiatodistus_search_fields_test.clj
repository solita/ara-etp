(ns solita.etp.service.energiatodistus-search-fields-test
  (:require
    [clojure.test :as t]
    [solita.etp.service.energiatodistus-search-fields :as search-fields]))

(t/deftest kasvihuonepaastot-per-nelio-sql-test
  (t/is (= (str "(coalesce(energiatodistus.t$kaytettavat_energiamuodot$kaukolampo, 0) * 0.059"
                " + coalesce(energiatodistus.t$kaytettavat_energiamuodot$sahko, 0) * 0.05"
                " + coalesce(energiatodistus.t$kaytettavat_energiamuodot$uusiutuva_polttoaine, 0) * 0.027"
                " + coalesce(energiatodistus.t$kaytettavat_energiamuodot$fossiilinen_polttoaine, 0) * 0.306"
                " + coalesce(energiatodistus.t$kaytettavat_energiamuodot$kaukojaahdytys, 0) * 0.014)"
                " / nullif(energiatodistus.lt$lammitetty_nettoala, 0)")
           (-> search-fields/computed-fields
               :energiatodistus :tulokset :kasvihuonepaastot-per-nelio first))))
