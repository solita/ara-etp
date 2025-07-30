(ns solita.etp.security-test
  (:require [clojure.test :as t]
            [solita.etp.security :as security]))

(t/deftest log-safe-henkilotunnus-test
  (t/is (= "" (security/log-safe-henkilotunnus nil)))
  (t/is (= "0101" (security/log-safe-henkilotunnus "0101")))
  (t/is (= "010101A****" (security/log-safe-henkilotunnus "010101A000A"))))

(t/deftest relative-redirect-url-test
  (t/is (= (solita.etp.security/->relative-redirect-url "https://etp.example.com")
           "/"))
  (t/is (= (solita.etp.security/->relative-redirect-url "https://etp.example.com/")
           "/"))
  (t/is (= (solita.etp.security/->relative-redirect-url "https://etp.example.com/f")
           "/f"))
  (t/is (= (solita.etp.security/->relative-redirect-url "https://etp.example.com/f/")
           "/f/"))
  (t/is (= (solita.etp.security/->relative-redirect-url "https://etp.example.com/f?qa=1&qb=2#ha=3&hb=4")
           "/f?qa=1&qb=2#ha=3&hb=4"))

  (t/is (= (security/->relative-redirect-url "https://etp.example.com//foo")      "/"))
  (t/is (= (security/->relative-redirect-url "//evil.com/")                       "/"))

  ;; path supplied without the leading “/” gets prepended with one
  (t/is (= (security/->relative-redirect-url "https://etp.example.com/foo/bar")   "/foo/bar"))

  ;; ---------------- query / fragment preservation --------
  (t/is (= (security/->relative-redirect-url "https://etp.example.com/?q=1")      "/?q=1"))
  (t/is (= (security/->relative-redirect-url "https://etp.example.com/#/route")   "/#/route"))
  (t/is (= (security/->relative-redirect-url "https://etp.example.com/f?x=1#frag")
           "/f?x=1#frag"))
  ;; encoded octets must survive untouched
  (t/is (= (security/->relative-redirect-url "https://etp.example.com/f?x=%2F%3F")
           "/f?x=%2F%3F"))

  ;; ---------------- odd-but-legal inputs -----------------
  ;; dot-segments are left as-is (browser resolves them client-side)
  (t/is (= (security/->relative-redirect-url "https://etp.example.com/../admin")
           "/../admin"))

  ;; ---------------- malformed URI → exception ------------
  ;; helper should bubble the URISyntaxException because we removed try/catch
  (t/is (thrown? java.net.URISyntaxException
                 (security/->relative-redirect-url "https://%ZZ"))))
