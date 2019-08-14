(ns getgerrit.core
  (:gen-class)
  (:require
   [clojure.java.io :as io]
   [clojure.string :as string]
   [clj-http.client :as http]
   [clj-jgit.porcelain :as git]
   [cheshire.core :refer :all]
   [clojure.tools.cli :refer [cli]]))

;; response strucuture
;; :policy/gui {
;;              :id "policy%2Fgui", 
;;              :state "READ_ONLY", 
;;              :web_links [
;;                          {
;;                           :name "gitweb", 
;;                           :url "https://gerrit.onap.org/r/gitweb?p=policy%2Fgui.git;a=summary"
;;                           }
;;                          ]}

(def api-url "https://gerrit.onap.org/r/")
(def projects-url-segment "projects/")


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; functions                                            ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; safe logging and printing to use instead of println
(defn safe-println [& more]
  (.write *out* (str (clojure.string/join " " more) "\n")))

(defn get-rid-of-magic-prefix [json-string]
  (subs json-string 4))

;; call Gerrit API and retreive projects list
(defn get-projects [base-url projects-url]
  (get-rid-of-magic-prefix
    ((http/get
       (str base-url projects-url) {:accept :json}) :body)))

;; parse json string to clojure structure
(defn parse-projects [projects]
  (into
   (sorted-map)
   (parse-string projects false)))

;; get only the names of projects
(defn get-projects-names [parsed-projects]
  (keys parsed-projects))

;; get selected project field
(defn get-project-detail [project project-data]
  (project project-data))

;; replace slash in the string by dot
(defn replace-slash-to-dot [string-w-slashes]
  (string/replace string-w-slashes #"/" "."))

;; git clone remote repo to local-dir
(defn clone-repo [git-url local-dir]
  (git/git-fetch-all 
   (git/git-clone git-url local-dir)))

(defn clone-project [project-name dest-dir-name]
  (printf "Cloning '%s' project to the %s..." project-name dest-dir-name)
  (println)
  (let [git-url (str api-url project-name)]
    (try
      (clone-repo git-url 
                  (.getPath 
                   (io/file dest-dir-name 
                            (replace-slash-to-dot project-name))))
      (catch Exception e
        (printf "Something went wrong while cloning project %s: %s ..."
                project-name (.getMessage e))
        (println)))
    (printf "Project '%s' cloned to '%s' ..." project-name dest-dir-name)
    (println)))

;; (defn make-dir [root dir-name]
;;   (println (str root projects-url-segment dir-name))
;;    (.mkdirs (io/file root projects-url-segment dir-name)))

;; (defn mkdir-in-base-dir [dir-name]
;;   (make-dir base-dir dir-name))

(defn parse-cli-args [args]
  (let [[opts args banner] (cli args
                                ["-h" "--help" "Print this help and quits."
                                 :default false :flag true]
                                ["-d" "--base-dir" "Base directory to clone projects into."
                                 :default "./" :flag false :parse-fn #(str %)]
                                ["-p" "--project-name" "Project name."
                                 :default nil :flag false :parse-fn #(str %)])]
    (when (:help opts)
      (println banner)
      (System/exit 0))
    opts))

(defn filter-projects-by-name [all-projects project-name]
  (if (not (string/blank? project-name))
    (filter #(string/starts-with? % project-name) all-projects)
    all-projects))

(defn -main [& args]
  (let [opts (parse-cli-args args)]
    (println opts)
    (let [projects-names (filter-projects-by-name
                          (->
                           (get-projects api-url projects-url-segment)
                           (parse-projects)
                           (get-projects-names))
                          (:project-name opts))]
      (dorun (->> projects-names
                  (map #(clone-project % (:base-dir opts)) ,,,))))))

;;
;; here goes different temporary repl tests
;;
(comment
  (->
   (get-projects api-url projects-url-segment) 
   (parse-projects)))

(comment
  (map get-project-details
       (vals
        (parse-projects
         (get-projects api-url projects-url-segment)))))

(comment
  (get-projects api-url projects-url-segment))

(comment
  (clone-repo
    (str api-url "aaf/authz")
    (str base-dir "aaf/authz")))

(comment
  (->> (str api-url "aaf/authz")
       (clone-repo (str api-url "aaf/authz") "/tmp/projects/aaf.authz")))

(comment
  (->>
   (->
    (get-projects api-url projects-url-segment)
    (parse-projects ,,,)
    (vals ,,,))
   (map get-project-details ,,,)))

(comment
  (->
   (get-projects api-url projects-url-segment)
   (parse-projects ,,,)
   (keys ,,, )))