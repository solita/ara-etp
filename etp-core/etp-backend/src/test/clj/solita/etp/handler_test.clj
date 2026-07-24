(ns solita.etp.handler-test
  (:require [clojure.test :as t]
            [clojure.string :as str]
            [ring.mock.request :as mock]
            [solita.etp.test-system :as ts]
            [solita.etp.test-data.laatija :as laatija-test-data]))

(t/use-fixtures :each ts/fixture)

(defn- logout-request []
  (-> (mock/request :get "/api/logout")
      laatija-test-data/with-suomifi-laatija))

(defn- whoami-request []
  (-> (mock/request :get "/api/private/whoami")
      laatija-test-data/with-suomifi-laatija
      (mock/header "Accept" "application/json")))

(defn- cleared-session-cookies? [response]
  (let [set-cookie (get-in response [:headers "Set-Cookie"])
        set-cookie-str (if (coll? set-cookie) (str/join ";" set-cookie) (str set-cookie))]
    (and (str/includes? set-cookie-str "AWSELBAuthSessionCookie-0=")
         (str/includes? set-cookie-str "AWSELBAuthSessionCookie-1=")
         (str/includes? set-cookie-str "Max-Age=0"))))

(t/deftest logout-redirects-and-clears-cookies-test
  (laatija-test-data/insert-suomifi-laatija!)
  (let [response (ts/handler (logout-request))]
    (t/testing "pre-existing behavior is unchanged: 302 redirect + AWSELBAuthSessionCookie-0/-1 cleared"
      (t/is (= 302 (:status response)))
      (t/is (contains? (:headers response) "Location"))
      (t/is (true? (cleared-session-cookies? response))))))

(t/deftest logout-without-any-jwt-degrades-gracefully-test
  (t/testing "no cookie/JWT at all -> still 302 + cookies cleared, stamping is a no-op (not an error)"
    (let [response (ts/handler (mock/request :get "/api/logout"))]
      (t/is (= 302 (:status response)))
      (t/is (true? (cleared-session-cookies? response))))))

(t/deftest logout-with-jwt-not-matching-any-kayttaja-degrades-gracefully-test
  (t/testing "JWT verifies but resolves to no existing kayttaja -> still 302 + cookies cleared"
    ;; laatija-test-data/with-suomifi-laatija's JWT is valid but no
    ;; matching kayttaja row has been inserted in this test, so it
    ;; cannot resolve to a whoami/kayttaja.
    (let [response (ts/handler (logout-request))]
      (t/is (= 302 (:status response)))
      (t/is (true? (cleared-session-cookies? response))))))

(t/deftest logout-then-replayed-cookie-is-rejected-test
  (laatija-test-data/insert-suomifi-laatija!)
  (t/testing "before logout, the private whoami endpoint succeeds"
    (t/is (= 200 (:status (ts/handler (whoami-request))))))

  (t/testing "/logout stamps logged_out_at (in addition to its pre-existing redirect/cookie-clear behavior)"
    (let [response (ts/handler (logout-request))]
      (t/is (= 302 (:status response)))))

  (t/testing "replaying the same (now revoked) JWT against a private endpoint is rejected with 401"
    (t/is (= 401 (:status (ts/handler (whoami-request)))))))

(t/deftest public-routes-are-unaffected-by-logout-test
  (let [response (ts/handler (mock/request :get "/api/public/kunnat"))]
    (t/is (= 200 (:status response)))))
