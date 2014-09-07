(ns topix-crawler.core
  (:require [clojure.string :as string]
            [clojure.tools.cli :refer [parse-opts]]
            [topix-crawler.list :as list]
            [topix-crawler.extract-topic-names :as extract]
            [topix-crawler.index-page-crawler :as index-crawler]
            [topix-crawler.extract-post-uris :as extract-post-uris]))

(def cmdline-options
  [["-c" "--crawl" "Crawl bro"]
   ["-e" "--extract C" "Extract topix forums from corpus-file"]
   ["-i" "--index F" "Step through index pages"]
   [nil "--write-to F" "File to write to"]
   [nil "--topic-uris T" "Index pages file"]])

(def topix-all-forums-uri "http://www.topix.com/forum/dir")

(defn -main
  [& args]
  (let [options (:options
                 (parse-opts args cmdline-options))]
    (cond (:crawl options)
          (list/crawl topix-all-forums-uri)

          (:extract options)
          (extract/process-dir-corpus (:extract options))

          (:index options)
          (let [index-pages (string/split-lines
                             (slurp (:index options)))]
            (doseq [uri index-pages]
              (index-crawler/crawl uri
                                   (:write-to options))))

          (:topic-uris options)
          (let [index-pages-file (:topic-uris options)]
            (extract-post-uris/index-page-records index-pages-file)))))
