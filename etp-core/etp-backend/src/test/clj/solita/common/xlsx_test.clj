(ns solita.common.xlsx-test
  (:require [clojure.test :as t]
            [solita.common.xlsx :as xlsx]))

(t/deftest fill-sheet!-test
  (let [xlsx (xlsx/create-xlsx)
        sheet (xlsx/create-sheet xlsx "Sheet 1")]
    (xlsx/fill-sheet! xlsx
                      sheet
                      [["A" "B" "C" nil "D"]
                       ["foo" "bar" nil nil nil "baz"]
                       [nil {:v "This is a long long text"
                             :align :right}]]
                      [100 300 100 100 100 100])
    (t/is (= "A" (-> sheet (xlsx/get-row 0) (xlsx/get-cell 0) xlsx/get-cell-value)))
    (t/is (= "baz" (-> sheet (xlsx/get-row 1) (xlsx/get-cell 5) xlsx/get-cell-value)))
    (t/is (= "This is a long long text"
             (-> sheet (xlsx/get-row 2) (xlsx/get-cell 1) xlsx/get-cell-value)))))
