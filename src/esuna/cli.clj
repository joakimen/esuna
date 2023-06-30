(ns esuna.cli
  "handles cli args and dispatching to internal functions"
  (:require [babashka.cli :as cli]
            [clojure.string :as str]
            [esuna.api :as api]))

(defn- format-commands-for-print [m]
  (let [cmds (->> (map :cmds m)
                  (filter not-empty)
                  (map #(str/join " " %))
                  sort)
        max-cmd-width (apply max (map count cmds))
        fmt-string (str "  %-" max-cmd-width "s")]
    (mapv #(format fmt-string %) cmds)))

(defn- print-help [m]
  (let [cmds (str/join "\n" (format-commands-for-print m))]
    (println "Usage: esu <command>
 ")
    (println cmds)))

(def table [{:cmds ["repo" "clone"] :fn api/clone-repos}
            {:cmds ["repo" "status"] :fn api/list-repo-status}
            {:cmds ["repo" "pull"] :fn api/pull-repos}
            {:cmds ["build"] :fn api/build-project}
            {:cmds ["dlq" "list"] :fn api/list-dlq-messages}
            {:cmds ["todo" "list"] :fn api/list-todos}
            {:cmds ["jira" "issue"] :fn api/list-jira-issues}
            {:cmds [] :fn (fn [_] (print-help table))}])

(defn -main [& args]
  (cli/dispatch table args {}))
