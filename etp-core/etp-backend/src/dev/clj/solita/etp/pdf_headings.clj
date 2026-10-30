(ns solita.etp.pdf-headings
  "Development utility for inspecting the heading structure of tagged PDFs."
  (:require [clojure.java.io :as io])
  (:import [org.apache.pdfbox.pdmodel PDDocument]
           [org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure
            PDMarkedContentReference
            PDStructureElement
            PDStructureNode]
           [org.apache.pdfbox.text PDFTextStripper]))

(defn- structure-children [node]
  (when (instance? PDStructureNode node)
    (.getKids ^PDStructureNode node)))

(defn- structure-elements [node]
  (if (instance? PDStructureElement node)
    (cons node (mapcat structure-elements (structure-children node)))
    (mapcat structure-elements (structure-children node))))

(defn- page-number [document page]
  (when page
    (some (fn [[index candidate]]
            (when (.equals (.getCOSObject page) (.getCOSObject candidate))
              (inc index)))
          (map-indexed vector (.getPages document)))))

(defn- content-references [heading]
  (->> (.getKids ^PDStructureElement heading)
       (keep (fn [kid]
               (cond
                 (integer? kid) {:mcid kid}
                 (instance? PDMarkedContentReference kid)
                 {:mcid (.getMCID ^PDMarkedContentReference kid)
                  :page (some-> kid .getPage)}
                 :else nil)))
       vec))

(defn- heading? [structure-element]
  (boolean (re-matches #"H[1-6]" (.getStructureType ^PDStructureElement structure-element))))

(defn- heading-data [document structure-element]
  (let [type (.getStructureType ^PDStructureElement structure-element)
        page (.getPage ^PDStructureElement structure-element)
        text (or (.getActualText ^PDStructureElement structure-element)
                 (.getTitle ^PDStructureElement structure-element)
                 (.getAlternateDescription ^PDStructureElement structure-element))]
    (cond-> {:type type
             :level (parse-long (subs type 1))
             :text text
             :page (page-number document page)
             :content-references (content-references structure-element)}
      (nil? text) (dissoc :text))))

(defn- page-lines [document page]
  (let [lines (atom [])
        stripper (proxy [PDFTextStripper] []
                   (writeString [text positions]
                     (when (seq positions)
                       (let [position (first positions)]
                         (swap! lines conj
                                {:text text
                                 :font-size (.getFontSizeInPt position)
                                 :font-name (.getName (.getFont position))
                                 :y (.getYDirAdj position)})))))]
    (.setStartPage stripper page)
    (.setEndPage stripper page)
    (.getText stripper document)
    @lines))

(defn- heading-line? [{:keys [text font-size font-name]}]
  (and (seq text)
       (< font-size 100)
       (not (re-find #":" text))
       (not (re-find #"Todistustunnus|Perusparannuspassin tunnus" text))
       (or (> font-size 12)
           (re-find #"Bold" font-name))))

(defn- page-heading-texts [document page headings]
  (let [lines (page-lines document page)
        candidates (->> lines
                        (filter heading-line?)
                        (sort-by :y))
        sizes (->> candidates
                   (map :font-size)
                   distinct
                   sort
                   reverse)
        by-size (fn [size]
                  (filter #(= size (:font-size %)) candidates))
        headings-by-level (group-by :level headings)
        level->size (zipmap (sort (keys headings-by-level)) sizes)
        assigned (atom {})]
    (doseq [[level level-headings] (sort-by key headings-by-level)]
      (let [size (get level->size level)]
        (doseq [[heading line] (map vector
                                    (sort-by :page level-headings)
                                    (by-size size))]
          (swap! assigned assoc heading (:text line)))))
    (mapv #(if-let [text (get @assigned %)]
             (assoc % :text text)
             %)
          headings)))

(defn- add-visual-text [document headings]
  (->> headings
       (group-by :page)
       (sort-by key)
       (mapcat (fn [[page page-headings]]
                 (page-heading-texts document page page-headings)))
       vec))

(defn- add-heading [tree stack heading]
  (let [level (:level heading)
        stack (->> stack
                   (take-while #(< (:level %) level))
                   vec)
        parent (peek stack)
        parent-path (:path parent)
        path (if parent
               (conj parent-path :children (count (get-in tree (conj parent-path :children))))
               [(count tree)])
        tree (if parent
               (update-in tree (conj parent-path :children) (fnil conj []) heading)
               (conj tree heading))]
    [tree (conj stack {:level level :path path})]))

(defn extract-heading-tree
  "Extract a nested heading tree from a tagged PDF.

   The returned value is a vector of maps with :type, :level, :page and
   :content-references keys. Text is read from the visible text lines on each
   page and matched to heading levels using the rendered font sizes. The
   content references are retained as the structural provenance."
  [pdf]
  (with-open [document (PDDocument/load (io/file pdf))]
    (let [root (.getStructureTreeRoot (.getDocumentCatalog document))
          headings (->> (structure-elements root)
                        (filter heading?)
                        (map #(heading-data document %))
                        (add-visual-text document))
          [tree _] (reduce (fn [[tree stack] heading]
                             (add-heading tree stack heading))
                           [[] []]
                           headings)]
      {:file (.getPath (io/file pdf))
       :tagged? (some? root)
       :headings tree})))
