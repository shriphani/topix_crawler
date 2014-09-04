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
     (if (empty? queue)
       (println :done)
       (let [start (first queue)
             _     (println :downloading start)
             body  (:body
                    (client/get start
                                {:headers
                                 {"User-Agent"
                                  (str "Mozilla/5.0 (Windows NT 6.1;) Gecko/20100101 Firefox/13.0.1"
                                       " lemurproject crawler, version 1.0"
                                       " contact: http://boston.lti.cs.cmu.edu/clueweb12/")}}))
             
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
             
             resolved-links (filter
                             (fn [l]
                               (re-find #"/forum/" l))
                             (map
                              (fn [l]
                                (uri/resolve-uri start l))
                              list-links))
             
             new-queue (concat (rest queue)
                               (clojure.set/difference (set resolved-links)
                                                       (set queue)
                                                       (set crawled)))
             
             new-crawled (clojure.set/union crawled
                                            (set [start]))]
         (do (write-bodies start body)
             (Thread/sleep (+ 1500 (rand-int 7000)))
             (recur new-queue new-crawled))))))
