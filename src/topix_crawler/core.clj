(ns topix-crawler.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [topix-crawler.list :as list]
            [topix-crawler.extract-topic-names :as extract]))

(def cmdline-options
  [["-c" "--crawl" "Crawl bro"]
   ["-e" "--extract C" "Extract topix forums from corpus-file"]])

(def topix-all-forums-uri "http://www.topix.com/forum/dir")

(defn -main
  [& args]
  (let [options (:options
                 (parse-opts args cmdline-options))]
    (cond (:crawl options)
          (list/crawl topix-all-forums-uri)

          (:extract options)
          (extract/process-dir-corpus (:extract options)))))
