(ns solita.etp.test-api
  (:require [ring.mock.request :as mock]
            [solita.etp.handler :as handler]
            [solita.etp.test-system :as ts]))

(def ^:private access-token
  "eyJraWQiOiJ0ZXN0LWtpZCIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJwYWFrYXl0dGFqYUBzb2xpdGEuZmkiLCJ0b2tlbl91c2UiOiJhY2Nlc3MiLCJzY29wZSI6Im9wZW5pZCIsImF1dGhfdGltZSI6MTU4MzIzMDk2OSwiaXNzIjoiaHR0cHM6Ly9yYXcuZ2l0aHVidXNlcmNvbnRlbnQuY29tL3NvbGl0YS9ldHAtY29yZS9mZWF0dXJlL0FFLTQzLWF1dGgtaGVhZGVycy1oYW5kbGluZy9ldHAtYmFja2VuZC9zcmMvbWFpbi9yZXNvdXJjZXMiLCJleHAiOjE4OTM0NTYwMDAsImlhdCI6MTU4MzQxMzQyNCwidmVyc2lvbiI6MiwianRpIjoiNWZkZDdhMjktN2VlYS00ZjNkLWE3YTYtYzIyODQyNmY2MTJiIiwiY2xpZW50X2lkIjoidGVzdC1jbGllbnRfaWQiLCJ1c2VybmFtZSI6InRlc3QtdXNlcm5hbWUifQ.PY5_jWcdxhCyn2EpFpss7Q0R3_xH1PvHi4mxDLorpppHnciGT2kFLeutebi7XeLtTYwmttTxxg2tyUyX0_UF7zj_P-tdq-kZQlud1ENmRaUxLXO5mTFKXD7zPb6BPFNe0ewRQ7Uuv3lDk_IxOf-6i86VDYB8luyesEXq7ra4S4l8akFodW_QYBSZQnUva_CVyzsTNcmgGTyrz2NI6seT1x6Pt1uFdYI97FHKlCCWVL1Z042omfujfta8j8XkTWdhKf3dfsHRWjrw31xqOkgD7uwPKcrC0U-wIj3U0uX0Rz2Tk4T-kIq4XTkKttYpkJqOmMFAYuhk6MDjfRkPWBZhUA")

(def ^:private oidc-data
  "eyJ0eXAiOiJKV1QiLCJraWQiOiJ0ZXN0LWtpZCIsImFsZyI6IlJTMjU2IiwiaXNzIjoidGVzdC1pc3MiLCJjbGllbnQiOiJ0ZXN0LWNsaWVudCIsInNpZ25lciI6InRlc3Qtc2lnbmVyIiwiZXhwIjoxODkzNDU2MDAwfQ.eyJzdWIiOiJwYWFrYXl0dGFqYUBzb2xpdGEuZmkiLCJjdXN0b206VklSVFVfbG9jYWxJRCI6InZ2aXJrYW1pZXMiLCJjdXN0b206VklSVFVfbG9jYWxPcmciOiJ0ZXN0aXZpcmFzdG8uZmkiLCJ1c2VybmFtZSI6InRlc3QtdXNlcm5hbWUiLCJleHAiOjE4OTM0NTYwMDAsImlzcyI6InRlc3QtaXNzIn0.BfuDVOFUReiJd6N05Re6affps_47AA0F5o-g6prmXgAnk4lB1S3k9RpovCFU3-R5Zn0p38QTiwi5dENHCHaj1A6MGHHKeYd7vBZK0VquuBxlIQH-4k1MWLvpYnkK3yuEvfmbRb3jYspCA_4N-AF21cCyjd15RiuIawLCEM0Km1DRgLhXIBta6XCGSRwaRmrT7boDRMp7hUkYPpoakCahMC70sjyuvLE0pjAy1_S09g4SkboentI7WhfsfN4uAHbKy6ViVMfsnwVVvKsM8dXav_a-6PoNGywuUbi8nHt8c20KiB_AzAEYSqxbRX1YBd0UHlYS16LbLtMBTOctCBLDMg")


(defn with-virtu-user
  "Adds headers to the given requests to authenticate the
  request as pääkäyttäjä created with solita.etp.test-data.kayttaja/insert-virtu-paakayttaja!.
  User needs to be created first in the test."
  [request]
  (-> request
      (mock/header "x-amzn-oidc-accesstoken" access-token)
      (mock/header "x-amzn-oidc-identity" "paakayttaja@solita.fi")
      (mock/header "x-amzn-oidc-data" oidc-data)))

(defn handler
  "Mimics real handler usage with test assets"
  [req]
  (handler/handler (merge req {:db            ts/*db*
                               :aws-s3-client ts/*aws-s3-client*})))
