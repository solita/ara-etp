(ns solita.etp.schema.asha
  (:require [schema.core :as schema]))

(def CaseCreateResponse
  {:id          schema/Int
   :case-number schema/Str})

(def CaseInfoResponse
  {:id             schema/Int
   :case-number    schema/Str
   :status         schema/Str
   :classification schema/Str
   :name           schema/Str
   :description    schema/Str
   :created        schema/Str})

(def ActionInfoAction
  {:object-class         (schema/maybe schema/Str)
   :id                   (schema/maybe schema/Int)
   :version              (schema/maybe schema/Int)
   :contacting-direction (schema/maybe schema/Str)
   :name                 (schema/maybe schema/Str)
   :description          (schema/maybe schema/Str)
   :status               (schema/maybe schema/Str)
   :created              (schema/maybe schema/Str)})

(def ActionInfoResponse
  {:processing-action                       ActionInfoAction
   (schema/optional-key :assignee)          (schema/maybe schema/Str)
   :queue                                   schema/Int
   (schema/optional-key :selected-decision) {:decision               (schema/maybe schema/Str)
                                             :next-processing-action ActionInfoAction}})