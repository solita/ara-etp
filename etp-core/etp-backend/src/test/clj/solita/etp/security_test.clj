(ns solita.etp.security-test
  (:require [clojure.test :as t]
            [solita.etp.security :as security]
            [solita.etp.api.response :as response])
  (:import (java.time Instant)))

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

(t/deftest logged-out?-test
  (let [now (Instant/now)]

    (t/testing "logged-out-at is nil (user has never logged out) -> request is valid regardless of auth-time"
      (t/is (false? (security/logged-out? now nil)))
      (t/is (false? (security/logged-out? (.minusSeconds now 100000) nil))))

    (t/testing "auth-time strictly before logged-out-at -> revoked"
      (t/is (true? (security/logged-out? (.minusSeconds now 10) now))))

    (t/testing "auth-time strictly after logged-out-at -> valid (logged out then logged back in)"
      (t/is (false? (security/logged-out? now (.minusSeconds now 10)))))

    (t/testing "boundary condition: auth-time exactly equal to logged-out-at -> treated as still valid"
      (t/is (false? (security/logged-out? now now))))

    (t/testing "sub-second clock skew: logged-out-at stamped with sub-second precision after auth-time's truncated second must not be incorrectly treated as revoked"
      ;; logged_out_at stamped at e.g. 12:00:00.900, auth_time truncated
      ;; by Cognito to 12:00:00 - the same login/logout instant should
      ;; not be considered a revoked session merely due to sub-second
      ;; resolution differences.
      (let [logged-out-at (.plusMillis now 900)
            auth-time (.truncatedTo now java.time.temporal.ChronoUnit/SECONDS)]
        (t/is (false? (security/logged-out? auth-time logged-out-at)))))

    (t/testing "auth-time far in the past with a recent logged-out-at -> revoked"
      (t/is (true? (security/logged-out? (.minusSeconds now (* 3600 24 365)) now))))

    (t/testing "auth-time far in the future relative to logged-out-at (defensive clock skew case) -> valid"
      (t/is (false? (security/logged-out? (.plusSeconds now (* 3600 24 365)) now))))))

(t/deftest wrap-reject-if-logged-out-test
  (let [now (Instant/now)
        ok-response {:status 200 :body "ok"}
        handler (fn [_req] ok-response)]

    (t/testing "logged-out-at is nil (user never logged out) -> downstream handler is invoked"
      (let [req {:jwt-payloads {:access {:auth_time (.getEpochSecond now)}}
                 :logged-out-at nil}]
        (t/is (= ok-response ((security/wrap-reject-if-logged-out handler) req)))))

    (t/testing "auth-time after logged-out-at -> downstream handler is invoked"
      (let [logged-out-at (.minusSeconds now 3600)
            req {:jwt-payloads {:access {:auth_time (.getEpochSecond now)}}
                 :logged-out-at logged-out-at}]
        (t/is (= ok-response ((security/wrap-reject-if-logged-out handler) req)))))

    (t/testing "auth-time before logged-out-at -> request is rejected with exactly response/unauthorized, downstream handler is not invoked"
      (let [logged-out-at now
            auth-time (.minusSeconds now 3600)
            called? (atom false)
            spy-handler (fn [_req] (reset! called? true) ok-response)
            req {:jwt-payloads {:access {:auth_time (.getEpochSecond auth-time)}}
                 :logged-out-at logged-out-at}
            response ((security/wrap-reject-if-logged-out spy-handler) req)]
        (t/is (= response/unauthorized response))
        (t/is (false? @called?))))

    (t/testing "does not throw for well-formed requests where whoami-derived pieces are otherwise minimal"
      (let [req {:jwt-payloads {:access {:auth_time (.getEpochSecond now)}}
                 :logged-out-at nil}]
        (t/is (some? ((security/wrap-reject-if-logged-out handler) req)))))))
