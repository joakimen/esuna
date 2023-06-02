(ns esuna.api
  "core functionality of the project"
  (:require [babashka.fs :as fs]
            [clojure.string :as str]
            [esuna.env :refer [env]]
            [esuna.lib.git :as git]))

(defn clone-repos [_]
  (println "cloning repos ...")
  (let [orgs (:git-repos env)
        repos (->> orgs
                   (pmap git/list-repos) doall flatten)]
    (println "found" (count repos) "repo(s) for orgs:" (str/join ", " orgs))
    (let [clone-results (->> repos (pmap #(git/clone (fs/expand-home (:git-root env)) %)) doall)
          successful-clones (->> clone-results
                                 (filter #(= (-> % :clone-result :status) 0))
                                 (map :repo))]
      (when-not (empty? successful-clones)
        (println "cloned" (count successful-clones) "new repos(s):")
        (run! #(println "-" %) successful-clones)))))
