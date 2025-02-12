(ns solita.etp.test-data.laatija
  (:require [ring.mock.request :as mock]
            [schema-tools.core :as st]
            [solita.etp.test-system :as ts]
            [solita.etp.test-data.generators :as generators]
            [solita.etp.schema.laatija :as laatija-schema]
            [solita.etp.service.kayttaja-laatija :as kayttaja-laatija-service]))

(defn generate-adds [n]
  (map #(generators/complete {:henkilotunnus %1
                              :email         %2
                              :patevyystaso  (rand-nth [1 2])}
                             laatija-schema/KayttajaLaatijaAdd)
       (generators/unique-henkilotunnukset n)
       (generators/unique-emails n)))

(defn dissoc-admin-update [update]
  (apply dissoc update (concat (keys laatija-schema/KayttajaAdminUpdate)
                               (keys laatija-schema/LaatijaAdminUpdate))))

(defn generate-updates [n include-admin-fields?]
  (map #(cond-> (generators/complete {:email         %2
                                      :henkilotunnus %1
                                      :patevyystaso  (rand-nth [1 2])
                                      :toimintaalue  (rand-nth (range 0 18))}
                                     laatija-schema/KayttajaLaatijaUpdate)
                (not include-admin-fields?) dissoc-admin-update)
       (generators/unique-henkilotunnukset n)
       (generators/unique-emails n)))

(defn insert! [kayttaja-laatija-adds]
  (kayttaja-laatija-service/upsert-kayttaja-laatijat! ts/*db* kayttaja-laatija-adds))

(defn generate-and-insert!
  ([] (first (generate-and-insert! 1)))
  ([n]
   (let [kayttaja-laatija-adds (generate-adds n)]
     (zipmap (insert! kayttaja-laatija-adds) kayttaja-laatija-adds))))

(defn insert-suomifi-laatija!
  "Inserts a new laatija that can be used with headers below and returns its id.
  Takes optional parameter of user-data, otherwise generated data will be used.

  Headers:
  x-amzn-oidc-accesstoken eyJraWQiOiJ0ZXN0LWtpZCIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJsYWF0aWphQHNvbGl0YS5maSIsInRva2VuX3VzZSI6ImFjY2VzcyIsInNjb3BlIjoib3BlbmlkIiwiYXV0aF90aW1lIjoxNTgzMjMwOTY5LCJpc3MiOiJodHRwczovL3Jhdy5naXRodWJ1c2VyY29udGVudC5jb20vc29saXRhL2V0cC1jb3JlL2ZlYXR1cmUvQUUtNDMtYXV0aC1oZWFkZXJzLWhhbmRsaW5nL2V0cC1iYWNrZW5kL3NyYy9tYWluL3Jlc291cmNlcyIsImV4cCI6MTg5MzQ1NjAwMCwiaWF0IjoxNTgzNDEzNDI0LCJ2ZXJzaW9uIjoyLCJqdGkiOiI1ZmRkN2EyOS03ZWVhLTRmM2QtYTdhNi1jMjI4NDI2ZjYxMmIiLCJjbGllbnRfaWQiOiJ0ZXN0LWNsaWVudF9pZCIsInVzZXJuYW1lIjoidGVzdC11c2VybmFtZSJ9.NGaJpuBOqzD49pB6Rhhek6L5tgXbj7ub6mwggpPSdpAlZ5NkT41Lbe2leJTWRZoopwyH6lc8hyyPSS6MBa6g3SzQB0l6tzb1JUDvaKY1ZYxvyIcGidi1NgeNRfzk36iWnHCDy7wlw2EKj7Lj3l4QPg1ntFa0e9OH2h24m1Z0jpmOVfN6Uw-b9A6jn91wHDNgbjPoEV60iXj0YwSJpgruUc6cEw-eojElwy9JAY9QJI7fJVR6jcinfBphP9-fwatuKFgY7bnnysOk_C5thDZl_IfAolq7o14qm4T-xtSa4lCRLf-Hgezvc39Y7cZdSmNpA9rjEBAtH3zNa6nJ0uaxYg
  x-amzn-oidc-identity laatija@solita.fi
  x-amzn-oidc-data eyJ0eXAiOiJKV1QiLCJraWQiOiJ0ZXN0LWtpZCIsImFsZyI6IlJTMjU2IiwiaXNzIjoidGVzdC1pc3MiLCJjbGllbnQiOiJ0ZXN0LWNsaWVudCIsInNpZ25lciI6InRlc3Qtc2lnbmVyIiwiZXhwIjoxODkzNDU2MDAwfQ.eyJzdWIiOiJsYWF0aWphQHNvbGl0YS5maSIsImN1c3RvbTpWSVJUVV9sb2NhbElEIjoidmxhYXRpamEiLCJjdXN0b206VklSVFVfbG9jYWxPcmciOiJ0ZXN0aXZpcmFzdG8uZmkiLCJ1c2VybmFtZSI6InRlc3QtdXNlcm5hbWUiLCJleHAiOjE4OTM0NTYwMDAsImlzcyI6InRlc3QtaXNzIn0.XQlLcxsQ1HZzkT6F0bziE_q9tzU0oOUq8qKu7ZImHRg_lknQC4KajonCD4kw9j3adl17pmNytTS4aPzPbos21jyUZr7v1dVw2Ah7XtS73x__WAXc485sr5pee6FJiAgYA6Mhm0dw9kn35lG57YCWVmKmb81MHLX7MwXCYISmfBWm2MsWqi9aB2Mwoe9HIgnHmXU7spHma0f5At5VKCtJ-_8YOxo2n8AKHjzGlEjdlOO-X9Zf-s2VhtD6NCCIU686jm9U4Qse0iO4r1yGyr92n_MABaICbwwwpYKh7dPUqt8rbCY8BWVBAxEzEIScBZSHPMl_L8RMFyaSAYHD2nD3Hg"
  ([]
   (insert-suomifi-laatija! (first (generate-adds 1))))
  ([user-data]
   (-> user-data
       (merge {:henkilotunnus "010469-999W"
               :passivoitu false
               :email "laatija@solita.fi"
               :rooli 0})
       vector
       insert!
       first)))

(def laatija-access-token
  "eyJraWQiOiJ0ZXN0LWtpZCIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJsYWF0aWphQHNvbGl0YS5maSIsInRva2VuX3VzZSI6ImFjY2VzcyIsInNjb3BlIjoib3BlbmlkIiwiYXV0aF90aW1lIjoxNzMzMjMwOTY5LCJpc3MiOiJodHRwczovL3Jhdy5naXRodWJ1c2VyY29udGVudC5jb20vc29saXRhL2V0cC1jb3JlL2ZlYXR1cmUvQUUtNDMtYXV0aC1oZWFkZXJzLWhhbmRsaW5nL2V0cC1iYWNrZW5kL3NyYy9tYWluL3Jlc291cmNlcyIsImV4cCI6MTg5MzQ1NjAwMCwiaWF0IjoxNzMzNDEzNDI0LCJ2ZXJzaW9uIjoyLCJqdGkiOiI1ZmRkN2EyOS03ZWVhLTRmM2QtYTdhNi1jMjI4NDI2ZjYxMmIiLCJjbGllbnRfaWQiOiJ0ZXN0LWNsaWVudF9pZCIsInVzZXJuYW1lIjoidGVzdC11c2VybmFtZSJ9.XlIKLrNyLlJAnVmBVI3SxGSdTb4CUAKvhaCnDQT44XPG4Ilu1cD6X5sCY6DyOrN6muaEAY6OSag_qmEU51o6Dl6qMaP5egHNYLnXF-HAHBUg2muZ7AI5sM2iZMYrRlimOQcdaWm_pHXExLIFmhcXuA5Lzsq4VinesuCBxSqyIjGXgCj-lwgMlwq4Nb9D2mAnQ_fw6R8iOf-bLwvJ4LHNRFNEGRzOF_CVsbRlifx0InaJjUDBYEHWzIiC8Wf7Yn60fG90nxjMybQ2Es_sx3c8Zy3NQSnFj6Kg-eI0onES0jatTbH4dFeXyf5vJ44FgFo2mgPiu5a4CQGxsdfX8y6BYQ")

(def laatija-oidc-data
  "eyJ0eXAiOiJKV1QiLCJraWQiOiJ0ZXN0LWtpZCIsImFsZyI6IlJTMjU2IiwiaXNzIjoidGVzdC1pc3MiLCJjbGllbnQiOiJ0ZXN0LWNsaWVudCIsInNpZ25lciI6InRlc3Qtc2lnbmVyIiwiZXhwIjoxODkzNDU2MDAwfQ.eyJzdWIiOiJsYWF0aWphQHNvbGl0YS5maSIsImN1c3RvbTpGSV9uYXRpb25hbElOIjoiMDEwNDY5LTk5OVciLCJ1c2VybmFtZSI6InRlc3QtdXNlcm5hbWUiLCJleHAiOjE4OTM0NTYwMDAsImlzcyI6InRlc3QtaXNzIn0.Uk3DCz8fVTqgE_ge0ywVYpeFXnt5x6orlE3cC1e3lgs_2tzv7WHKCtLSbMWXYrcwOgZ-eOOuF_StNovq-IyMVjKAGxu1qaAR20Q2AYYg3JnOUNj1YPBpyA1nF5FYeNDolhlQKxrCj07hXmSBxBeIqNgOnepRJ0Rx9QEBoGbLvzT9mBf_m7CZncTcg2PCdtXiNeww5fx0R2ip53BcdI5nYcKz_LOae6Y707vfbmgfV_zDTFATDAqquwNuhtsqXbmc6D9smkJOl7CNPXY4riDuqyCbi62JMme90HlcHBRnMDLJXEIkTCaox3vdztxBlYVQYUwsaV3eOdQ7_v3wOal18w")


;; Note: We don't actually have Virtu laatijas. Preferrably check other methods for generating laatijas.
(defn with-suomifi-laatija
  "Add virtu laatija user to ring-mock request"
  [request]
  (-> request
      (mock/header "x-amzn-oidc-accesstoken" laatija-access-token)
      (mock/header "x-amzn-oidc-identity" "laatija@solita.fi")
      (mock/header "x-amzn-oidc-data" laatija-oidc-data)))
