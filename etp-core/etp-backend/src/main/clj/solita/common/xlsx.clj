(ns solita.common.xlsx
  (:require [clojure.string :as str]
            [clojure.java.io :as io])
  (:import (org.apache.poi.ss.usermodel WorkbookFactory HorizontalAlignment)
           (org.apache.poi.ss.util CellAddress)
           (org.apache.poi.xssf.usermodel XSSFWorkbook XSSFWorkbookFactory XSSFFormulaEvaluator)))

;;
;; Workbook, loading, saving
;;
(WorkbookFactory/addProvider (XSSFWorkbookFactory.))

(defn create-xlsx []
  (WorkbookFactory/create (boolean true)))

(defn load-xlsx [^java.io.InputStream is]
  (WorkbookFactory/create is))

(defn save-xlsx [xlsx path]
  (with-open [os (io/output-stream path)]
    (.write xlsx os)))

;;
;; Sheets
;;

(defn get-sheet [xlsx idx]
  (.getSheetAt xlsx idx))

(defn create-sheet [xlsx label]
  (.createSheet xlsx label))

;;
;; Rows
;;

(defn get-row [sheet idx]
  (if-let [row (.getRow sheet idx)]
    row
    (.createRow sheet idx)))

;;
;; Cells
;;

(defn get-cell [row idx]
  (if-let [cell (.getCell row idx)]
    cell
    (.createCell row idx)))

(defn get-cell-value [cell]
  (let [v (.getStringCellValue cell)]
    (if (str/blank? v) nil v)))

(defn row-and-column-idx [address]
  (let [cell-address (CellAddress. ^String address)]
    {:row-idx (.getRow cell-address)
     :col-idx (.getColumn cell-address)}))

(defn get-cell-value-at [sheet address]
  (let [{:keys [row-idx col-idx]} (row-and-column-idx address)]
    (-> (get-row sheet row-idx)
        (get-cell col-idx)
        (get-cell-value))))

(defn cell-value [v]
  (cond
    (instance? java.time.LocalDate v) v
    (number? v) (double v)
    :else (str v)))

(defn set-cell-value [cell v]
  (->> v cell-value (.setCellValue cell)))

;;
;; Styles
;;

(defn create-bold-font [xlsx]
  (doto (.createFont xlsx)
    (.setBold true)))

(defn create-style-with-font [xlsx font]
  (doto (.createCellStyle xlsx)
    (.setFont font)))

(defn create-style-with-format [xlsx format]
  (doto (.createCellStyle xlsx)
    (.setDataFormat (-> xlsx
                        .getCreationHelper
                        .createDataFormat
                        (.getFormat format)))))

(defn create-style-with-align [xlsx align]
  (doto (.createCellStyle xlsx)
    (.setAlignment (case align
                     :center HorizontalAlignment/CENTER
                     :left HorizontalAlignment/LEFT
                     :right HorizontalAlignment/RIGHT))))

(defn set-column-width [sheet idx width]
  (.setColumnWidth sheet idx width))

(defn set-sheet-landscape [sheet landscape?]
  (-> sheet (.getPrintSetup) (.setLandscape landscape?)))

;;
;; Formulas
;;

(defn evaluate-formulas [^XSSFWorkbook xlsx]
  (XSSFFormulaEvaluator/evaluateAllFormulaCells xlsx))

;;
;; Utility for filling a sheet from vectors
;;

(defn fill-sheet! [xlsx sheet row-vectors column-widths]
  (let [style-center (create-style-with-align xlsx :center)
        style-left (create-style-with-align xlsx :left)
        style-right (create-style-with-align xlsx :right)]
    (doseq [[row-idx row-vector] (map-indexed vector row-vectors)
            :let [row (get-row sheet row-idx)]]
      (doseq [[cell-idx cell-value-or-m] (map-indexed vector row-vector)
              :let [cell (get-cell row cell-idx)]]
        (if (map? cell-value-or-m)
          (do
            (set-cell-value cell (:v cell-value-or-m))
            (.setCellStyle cell (case (:align cell-value-or-m)
                                  :center style-center
                                  :right style-right
                                  style-left)))
          (set-cell-value cell cell-value-or-m))))
    (doseq [[col-idx width] (map-indexed vector column-widths)]
      (set-column-width sheet col-idx width))
    xlsx))
