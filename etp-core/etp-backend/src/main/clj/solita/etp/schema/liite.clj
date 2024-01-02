(ns solita.etp.schema.liite
  (:require [schema.core :as schema]
            [solita.etp.schema.common :as common-schema]
            [reitit.ring.schema :as reitit-schema]))

(def Liite {:id              common-schema/Key
            :createtime      common-schema/Instant
            :author-fullname schema/Str
            :nimi            schema/Str
            :contenttype     (schema/maybe schema/Str)
            :url             (schema/maybe common-schema/Url)
            :deleted         schema/Bool})

(def LiiteLinkAdd {:nimi schema/Str
                   :url common-schema/Url})

(def MultipartFiles
  {:files (schema/conditional
            vector? [reitit-schema/TempFilePart]
            :else reitit-schema/TempFilePart)})

