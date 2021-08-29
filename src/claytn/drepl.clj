(ns claytn.drepl
  (:require
   [clojure.string :as s]
   [clojure.tools.cli :refer [parse-opts]]
   [clojure.java.shell :refer [sh]]
   [clojure.java.io :as io])
  (:gen-class))

(def DEFAULT_WORKSPACE "drepl")

(defn sh-with-io 
  "Wrapper around ProcessBuilder with inherited IO"
  [dir commands]
  (let [pb (ProcessBuilder. commands)]
    (.directory pb dir)
    (-> pb
        .inheritIO
        .start
        .waitFor)))

(defn delete-dir!
  "Recursively removes a directories contents and then the directory itself
   Basically rm -rf but system agnostic"
  [^java.io.File f]
  (if-not (.isDirectory f)
    (io/delete-file f)
    (do
      (doseq [file (.listFiles f)]
        (delete-dir! file))
      ;; After all directory files are removed
      ;; Delete the top level directory file itself
      (.delete f))))

(defn create-project-dir!
  "Attempts to create a project directory in system's default temp directory if the project does not already exist"
  [prefix]
  (let [path (str (System/getProperty "java.io.tmpdir") prefix)
        f (io/as-file path)]
    (if (.isDirectory f)
      f
      (if (.mkdir f)
        (do
          (println "Created project directory at: " (.getAbsolutePath f))
          f)
        (throw (Exception.
                (str
                 "Failed to create project directory on host machine at: " path)))))))

(defn create-npm-project!
  "Creates npm project in provided path if it doesn't already exist"
  [^java.io.File dir]
  (if-not (.isFile (io/as-file (str (.getAbsolutePath dir) "/package.json")))
    (sh "npm" "init" "-y" :dir dir)))

(defn install-dependencies!
  "Given an npm directory and list of dependencies, install required dependencies"
  [{:keys [^java.io.File dir deps]}]
  (let [ret-code (sh-with-io dir (concat ["npm" "i" "--save"] deps))]
    (if (not= 0 ret-code)
      (throw (Exception. "Failed to install dependencies")))))

(defn clean-npm-project! [^java.io.File dir]
  (if (.isDirectory dir)
    (do 
      (delete-dir! dir)
      (println "drepl npm repository cleaned"))
    (println "drepl npm repository does not exist yet")))

(defn run-node-repl! [{:keys [dir typescript?]}]
  (sh-with-io dir ["npx" (if typescript? "ts-node" "node")]))

(def cli-options [["-h" "--help"]
                  ["-t" "--typescript" "Use ts-node engine (not fully supported)"]
                  ["-c" "--clean" "Clean up all previously used dependencies"]])

(defn help-message
  [options-summary]
  (->> ["Welcome to drepl!"
        "The node repl with quick library installations"
        ""
        "Usage: drepl [options] [npm-package-names...]"
        ""
        "Dependencies:"
        "  npm & npx must be available in your $PATH"
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

(defn -main
  [& args]
  (let [{:keys [arguments options exit-message ok?]} (validate-args args)]
    (if exit-message
      (exit (if ok? 0 1) exit-message)

      (try
        (let [dir (create-project-dir! DEFAULT_WORKSPACE)]
          (cond
            (:clean options)
            (clean-npm-project! dir)

            :else
            (do
              (create-npm-project! dir)
              (install-dependencies! {:dir dir :deps arguments})
              (run-node-repl! {:dir dir :typescript? (:typescript options)}))))
        
        (catch Exception e
          (exit 1 (str "Error: " (.getMessage e))))))))

