(ns topix-crawler.list
  "Download listing"
  (:require [clj-http.client :as client]
            [clojure.java.io :as io]
            [net.cgrand.enlive-html :as html]
            (org.bovinegenius [exploding-fish :as uri]))
  (:import [java.io StringReader])
  (:use [clojure.pprint :only [pprint]]))

(def writer (io/writer (str "/bos/tmp19/spalakod/topix/"
                            "topix-list.corpus")
                       :append
                       true))

(defn write-bodies
  [uri body]
  (pprint {:uri  uri
           :body body}
          writer))

(defn crawl
  ([start]
     (crawl [start] (set [])))

  ([queue crawled]
     (let [start (first queue)
           _     (println start)
           body  (:body
                  (client/get start))

           anchors (-> body
                       (StringReader.)
                       html/html-resource
                       (html/select [:a]))

           links (map
                  (fn [a]
                    (-> a :attrs :href))
                  anchors)

           list-links (filter
                       (fn [l]
                         (and l
                              (or (re-find #"list$" l)
                                  (re-find #"list/$" l))))
                       links)
           
           resolved-links (map
                           (fn [l]
                             (uri/resolve-uri start l))
                           list-links)

           new-queue (concat (rest queue)
                             (clojure.set/difference (set resolved-links)
                                                     (set queue)
                                                     (set crawled)))
           
           new-crawled (clojure.set/union crawled
                                          (set [start]))]
       (do (write-bodies start body)
           (recur new-queue new-crawled)))))
