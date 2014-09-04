(ns topix-crawler.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [topix-crawler.list :as list]))

(def cmdline-options
  [["-c" "--crawl" "Crawl bro"]])

(def topix-all-forums-uri "http://www.topix.com/forum/dir")

(defn -main
  [& args]
  (let [options (:options
                 (parse-opts args cmdline-options))]
    (list/crawl topix-all-forums-uri)))
