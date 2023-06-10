(ns esuna.api
  "core functionality of the project"
  (:require [babashka.fs :as fs]
            [babashka.process :as p]
            [clojure.string :as str]
            [doric.core :as doric]
            [esuna.env :refer [env]]
            [esuna.lib.aws.sqs :as sqs]
            [esuna.lib.git :as git]))


(defn clone-repos [_]
  (println "cloning repos ...")
  (let [orgs (:git-orgs env)
        repos (->> orgs
                   (pmap git/list-remote-repos) doall flatten)]
    (println "found" (count repos) "repo(s) for orgs:" (str/join ", " orgs))
    (let [clone-results (->> repos (pmap #(git/clone (fs/expand-home (:git-root env)) %)) doall)
          successful-clones (->> clone-results
                                 (filter #(= (-> % :clone-result :status) 0))
                                 (map :repo))]
      (when-not (empty? successful-clones)
        (println "cloned" (count successful-clones) "new repos(s):")
        (run! #(println "-" %) successful-clones)))))

(defn list-dlq-messages [_]
  (->> (sqs/list-dead-letter-queues)
       (map #(assoc {} :DLQ (:QueueUrl %)
                    :Messages (-> %  :Attributes :ApproximateNumberOfMessages)))
       (sort-by :DLQ)
       (doric/table [:DLQ :Messages])
       (doall)
       (println)))


(defn list-repo-status [_]
  (let [dirty-repos (->> (git/list-local-repos)
                         (pmap #(assoc {} :repo % :status (git/status %)))
                         (filter #(seq (:status %)))
                         (doall))]
    (doseq [repo dirty-repos]
      (println)
      (p/shell "gum" "style" (:repo repo) "--bold" "--underline")
      (println (:status repo)))))
