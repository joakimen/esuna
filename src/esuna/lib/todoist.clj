(ns esuna.lib.todoist
  (:require [babashka.curl :as curl]
            [cheshire.core :as json]
            [esuna.env :refer [env]]))

(defn client []
  (:todoist (env)))

(defn ->edn [body]
  (json/parse-string body true))

(defn api-get [client url]
  (let [{:keys [api-url api-token]} client
        {:keys [status body]} (curl/get (str api-url "/" url)
                                        {:headers {"Authorization" (str "Bearer " api-token)}})]
    (when-not (= status 200)
      (throw (ex-info (str "API GET failed with status " status) {:babashka/exit 1})))
    (->edn body)))

(defn get-activity [client]
  (api-get client "sync/v9/activity/get"))

(defn get-projects [client]
  (api-get client "rest/v2/projects"))

(defn get-tasks [client]
  (api-get client "/rest/v2/tasks"))

(comment

  (def tasks
    (get-tasks client))

  (map (fn [{:keys [created_at content] {:keys [date]} :due}]
         (println {:created-at created_at
                   :content content
                   :due date})) tasks)
  ;;
  )
