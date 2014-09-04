(ns topix-crawler.index-page-crawler
  "Topix index page crawler"
  (:require [clj-http.client :as client]
            [clojure.java.io :as io]
            [net.cgrand.enlive-html :as html]
            (org.bovinegenius [exploding-fish :as uri]))
  (:import [java.io StringReader])
  (:use [clojure.pprint :only [pprint]]))

(defn write-bodies
  [writer uri body]
  (pprint {:uri  uri
           :body body}
          writer))

(defn crawl
  ([index-page-uri filename]
     (let [writer (io/writer filename :append true)]
       (crawl index-page-uri
              (set [])
              writer)))

  ([uri-to-get crawled writer]
     (println :downloading uri-to-get)
     (if uri-to-get
       (let [body (:body
                   (client/get
                    uri-to-get
                    {:headers
                     {"User-Agent"
                      (str "Mozilla/5.0 (Windows NT 6.1;) Gecko/20100101 Firefox/13.0.1"
                           " lemurproject crawler, version 1.0"
                           " contact: http://boston.lti.cs.cmu.edu/clueweb12/")}}))
             
             next-pg-anchor (-> body
                                (StringReader.)
                                html/html-resource
                                (html/select [:a.x-thread-pagination]))
             
             paginator (first
                        (filter
                         (fn [a]
                           (re-find #"Next" (html/text a)))
                         next-pg-anchor))
             
             next-uri-to-get
             (if paginator
               (uri/resolve-uri uri-to-get
                                (-> paginator
                                    :attrs
                                    :href))
               nil)]
         (do (write-bodies writer
                           uri-to-get
                           body)
             (Thread/sleep
              (+ 1500
                 (rand-int 7000)))
             (recur next-uri-to-get
                    (cons uri-to-get crawled)
                    writer)))
       (do (println :done)
           (.close writer)))))
