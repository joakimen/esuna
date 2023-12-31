(ns esuna.lib.git
  (:require [babashka.fs :as fs]
            [cheshire.core :as json]
            [clojure.string :as str]
            [esuna.lib.sh :refer [run run-with-result]]))

(defn ->edn [s]
  (json/parse-string s true))

(defn list-remote-repos [org]
  (->> (run "gh" "repo" "list" org
            "--json" "nameWithOwner"
            "--limit" "10000"
            "--no-archived")
       ->edn
       (map :nameWithOwner)))

(defn status [repo]
  (run "git" "-C" repo "status" "--porcelain"))

(defn clone [base-dir name-with-owner]
  (let [repo-dir (str (fs/path base-dir name-with-owner))
        clone-result (run-with-result "gh" "repo" "clone" name-with-owner repo-dir)]
    {:repo name-with-owner :clone-result clone-result}))

(defn list-local-repos []
  (str/split-lines (run "list-projects")))
