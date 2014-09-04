(ns topix-crawler.extract-topic-names
  "Extract topic names"
  (:require [net.cgrand.enlive-html :as html]
            (org.bovinegenius [exploding-fish :as uri]))
  (:import [java.io StringReader]))

(defn extract-forum-names
  [url body]
  (let [anchors (-> body
                    (StringReader.)
                    (html/html-resource)
                    (html/select [:ul.dir_col :a]))

        hrefs (map
               (fn [anchor]
                 (-> anchor :attrs :href))
               anchors)

        resolved-hrefs (map
                        (fn [l]
                          (uri/resolve-uri url l))
                        hrefs)

        forum-hrefs (filter
                     (fn [l]
                       (and (re-find #"/forum/" l)
                            (not
                             (or (re-find #"list$" l)
                                 (re-find #"list/$" l)))))
                     resolved-hrefs)]
    (doseq [l forum-hrefs]
      (println l))))
