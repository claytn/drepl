#!/usr/bin/env bb

(require
 '[babashka.process :refer [shell]]
 '[babashka.fs :as fs]
 '[clojure.string :as s]
 '[clojure.tools.cli :refer [parse-opts]])

(def workspace (fs/temp-dir))
(def workspace-prefix "drepl")

(def cli-options [["-h" "--help"]
                  ["-c" "--clean" "Clean up all previously used dependencies"]])

(defn help-message
  [options-summary]
  (->> ["Welcome to drepl!"
        "The node repl with quick library installations"
        ""
        "Usage: drepl [options] [npm-package-names...]"
        ""
        "Dependencies:"
        "  npm must be available in your $PATH"
        ""
        "Options:"
        options-summary
        ""]
       flatten
       (s/join "\n")))

(defn validate-args
  "Validate command line arguments. Either return a map indicating the program
  should exit (with a error message, and optional ok status), or a map
  indicating the action the program should take and the options provided."
  [args]
  (let [{:keys [options errors summary] :as parsed-opts} (parse-opts args cli-options)]
    (cond
      (:help options) ; help => exit OK with usage summary
      {:exit-message (help-message summary) :ok? true}

      errors
      {:exit-message errors}

      :else ;; Validation succeeded
      parsed-opts)))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn create-npm-project! []
  (let [dir (fs/create-temp-dir {:prefix workspace-prefix})]
    (shell {:dir (.toString dir)} "npm init -y")
    dir))

(defn clean-project! []
  (let [[project-dir] (fs/list-dir workspace (str workspace-prefix "*"))]
    (if (nil? project-dir)
      ;; If no project found - do nothing
      nil
      ;; Delete the project
      (fs/delete-tree (.toString project-dir)))
    (println "🧹 Cleaned all local drepl files")))

(defn run-drepl! [{:keys [deps]}]
  (try
    (let [[existing-project-dir] (fs/list-dir workspace (str workspace-prefix "*"))
          project-dir (if (nil? existing-project-dir)
                        (create-npm-project!)
                        existing-project-dir)
          project-path (.toString project-dir)] 
      
      (if (> (count deps) 0)
        ;; Install npm deps
        (shell {:dir project-path} (str "npm install --save " (s/join " " deps)))
        ;; No dependencies were specified
        (println "No dependencies installed"))

      ;; Start node repl
      (shell {:dir project-path} "node"))

    (catch Exception e
      (exit 1 (str "Error: " (.getMessage e))))))

(defn start! []
  (let [{:keys [arguments options exit-message ok?]} (validate-args *command-line-args*)]
    (cond
      exit-message  (exit (if ok? 0 1) exit-message)
      (:clean options) (clean-project!)
      :else (run-drepl! {:deps arguments}))
    
    nil))

(start!)
