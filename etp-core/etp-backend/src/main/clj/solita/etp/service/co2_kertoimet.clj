(ns solita.etp.service.co2-kertoimet
  "Energiamuotojen CO₂-päästökertoimet (kg CO₂/kWh).
   Ympäristöministeriön asetus 1048/2017.")

(def co2-kertoimet
  {:kaukolampo      0.059
   :sahko           0.05
   :uusiutuvat-pat  0.027
   :fossiiliset-pat 0.306
   :kaukojaahdytys  0.014})
