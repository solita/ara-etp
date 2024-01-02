(ns solita.etp.basic-auth-test
  (:require [clojure.test :as t]
            [solita.etp.basic-auth :as basic-auth]))

(def credentials "bGFhdGlqYTFAZXhhbXBsZS5jb206cGFzc3dvcmRjdXJyZW50bHlub3R1c2Vk")

(t/deftest basic-auth-id-and-password-test
  (t/is (nil? (basic-auth/req->id-and-password nil)))
  (t/is (nil? (basic-auth/req->id-and-password {:headers {}})))
  (t/is (nil? (basic-auth/req->id-and-password {:headers {"authorization" ""}})))
  (t/is (nil? (basic-auth/req->id-and-password
               {:headers {"authorization" credentials}})))
  (t/is (nil? (basic-auth/req->id-and-password
               {:headers {"authorization" (str "hello " credentials)}})))
  (t/is (= (basic-auth/req->id-and-password
            {:headers {"authorization" (str "Basic " credentials)}})
           {:id "laatija1@example.com"
            :password "passwordcurrentlynotused"})))
