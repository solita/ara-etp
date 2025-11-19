(ns solita.etp.jiralinks
  (:require [clj-http.client :as http])
  (:import (org.apache.commons.codec.binary Base64)))

(defn extract-jira-ids
  "Extract Jira ticket IDs matching AE-???? pattern from text."
  [text]
  (->> text
       (re-seq #"AE-\d+")
       (distinct)
       (map (fn [id]
              (let [[_ num] (re-matches #"AE-(\d+)" id)]
                {:id id :num (Integer/parseInt num)})))
       (sort-by :num)
       (map :id)))

(defn encode-b64 [s]
  (String. (Base64/encodeBase64 (.getBytes s "UTF-8"))))

(defn fetch-ticket-title
  "Fetch ticket title from Jira API. Returns nil if the request fails."
  [jira-url api-token ticket-id]
  (try
    (let [url (str jira-url "/rest/api/3/issue/" ticket-id)
          response (http/get url
                             {:headers {"Authorization" (str "Basic " (encode-b64 api-token))
                                        "Accept"        "application/json"}
                              :as      :json
                              :throw-exceptions false})]
      (if (= 200 (:status response))
        (get-in response [:body :fields :summary])
        (when-not (= 404 (:status response))
          ;; 404 is expected in case we scrape some false ticket IDs from the log
          (println "Warning: Failed to fetch ticket" url "- status:" (:status response))
          nil)))
    (catch Exception e
      (println "Warning: Error fetching ticket" ticket-id "-" (.getMessage e))
      nil)))

(defn format-markdown-link
  "Format a Jira ID as a Markdown link, optionally with title."
  ([jira-url ticket-id]
   (str "- [" ticket-id "](" jira-url "/browse/" ticket-id ")"))
  ([jira-url ticket-id title]
   (if title
     (str "- [" ticket-id "](" jira-url "/browse/" ticket-id ") - " title)
     (format-markdown-link jira-url ticket-id))))

(defn -main
  "Main function that reads git log from stdin and outputs Jira links as Markdown.

  Usage:
    Without API token: cat git.log | clj -M -m solita.etp.jiralinks https://example.atlassian.net
    With API token:    cat git.log | clj -M -m solita.etp.jiralinks https://example.atlassian.net YOUR_API_TOKEN"
  [& args]
  (when (empty? args)
    (println "Error: Please provide Jira workspace URL as argument")
    (println "Usage: cat git.log | clj -M -m solita.etp.jiralinks https://example.atlassian.net [API_TOKEN]")
    (System/exit 1))

  (let [jira-url (first args)
        api-token (second args)
        input (slurp *in*)
        ticket-ids (extract-jira-ids input)]
    (if (empty? ticket-ids)
      (println "No Jira tickets found matching AE-???? pattern")
      (doseq [ticket-id ticket-ids]
        (if api-token
          (when-let [title (fetch-ticket-title jira-url api-token ticket-id)]
            (println (format-markdown-link jira-url ticket-id title)))
          (println (format-markdown-link jira-url ticket-id)))))))
