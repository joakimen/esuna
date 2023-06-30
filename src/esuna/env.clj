(ns esuna.env)

(defn get-env [name]
  (or (System/getenv name)
      (throw (ex-info (str "env var " name " not set") {:babashka/exit 1}))))

;; move to XDG_CONFIG later
(defn env []
  {:git-root "~/dev/github.com"
   :git-orgs ["joakimen" "capralifecycle"]
   :todoist {:api-token (get-env "TODOIST_TOKEN")
             :api-url "https://api.todoist.com"}
   :jira {:api-token (get-env "JIRA_API_TOKEN")
          :api-user (get-env "JIRA_API_USER")
          :api-url (get-env "JIRA_HOST")}})
