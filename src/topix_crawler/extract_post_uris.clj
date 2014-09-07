(ns topix-crawler.extract-post-uris
  "Process index pages and get post URIs"
  (:require [clj-time.core :as t]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]
            [net.cgrand.enlive-html :as html]
            (org.bovinegenius [exploding-fish :as uri])
            [subotai.dates :as dates])
  (:import [java.io StringReader PushbackReader]))

(defn process-index-page
  [page-uri body]
  (let [tags  (-> body
                  (StringReader.)
                  (html/html-resource)
                  (html/select #{[:td :a.threadtitle] [:td.lut]}))]
    (map
     (fn [[uri-tag date-tag]]
       [(->> uri-tag
             :attrs
             :href
             (uri/resolve-uri page-uri))
        (html/text date-tag)])
     (partition 2 tags))))

(defn in-clueweb12pp-range?
  [a-date]
  (t/within?
   (t/interval
    (t/date-time 2012 02 10)
    (t/date-time 2012 05 10))
   a-date))

(defn index-page-records
  [index-pages-file]
  (let [rdr (-> index-pages-file io/reader (PushbackReader.))

        index-pg-records (take-while
                          identity
                          (repeatedly
                           (fn []
                             (try (read rdr)
                                  (catch Exception e nil)))))]
    (doseq [{pg-uri :uri body :body} index-pg-records]
      (doseq [[topic-uri date] (process-index-page pg-uri body)]
        (let [processed-date (first (dates/parse-date date))]
          (when (in-clueweb12pp-range? processed-date)
            (println topic-uri)))))))
