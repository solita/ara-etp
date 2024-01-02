(ns solita.common.logic
  "This namespace contains higher order logic functions.
  The purpose is to augment clojure core logic to ramda level.")

(defn if*
  "Higher order ramda style if function."
  ([predicate then-fn] (if* predicate then-fn (constantly nil)))
  ([predicate then-fn else-fn]
    #(if (predicate %) (then-fn %) (else-fn %))))

(defn when*
  "Higher order ramda style when function."
  ([predicate then-fn]
   #(if (predicate %) (then-fn %) %)))

(defn unless*
  "Higher order ramda style unless function."
  ([predicate then-fn]
   #(if (not (predicate %)) (then-fn %) %)))

(defn is-divisible-by [num divisor]
  (zero? (mod num divisor)))

(defn pipe
  "Function composition where functions are defined in reversed order.
   Same as ramda pipe."
  [& fns] (apply comp (reverse fns)))

(defn pred [binary-predicate fn value] #(binary-predicate (fn %) value))

(defmacro
  if-let*
  "This if-let allows multiple bindings.
  Multiple bindings are transformed to nested if-lets.
  Else expression can only refer to the first variable."
  ([bindings expr] `(if-let* ~bindings ~expr nil))
  ([bindings expr else]
   {:pre [(is-divisible-by (count bindings) 2)]}
   (if ((complement empty?) bindings)
     `(if-let [~(first bindings) ~(second bindings)]
        (if-let* ~(drop 2 bindings) ~expr ~else) ~else)
     expr)))