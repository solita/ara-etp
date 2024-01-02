(ns solita.common.map)

(defn map-keys [f m] (into {} (map (fn [[k, v]] [(f k) v]) m)))

(defn map-values [f m] (into {} (map (fn [[k, v]] [k (f v)]) m)))

(defn submap? [m1 m2] (= m1 (select-keys m2 (keys m1))))

(defmacro bindings->map [& bindings]
  (into {} (map (fn [s] [(keyword (name s)) s]) bindings)))

(defn paths
  ([coll]
   (paths coll []))
  ([coll path]
   (reduce
    (fn [acc [k v]]
      (let [v (if (sequential? v)
                (into {} (map-indexed vector v))
                v)
            current-path (conj path k)]
        (if (map? v)
          (concat acc (paths v current-path))
          (conj acc current-path))))
    []
    coll)))

(defn dissoc-in [m ks]
  (if (> (count ks) 1)
    (if (map? (get-in m (butlast ks)))
      (update-in m (butlast ks) dissoc (last ks))
      m)
    (dissoc m (first ks))))
