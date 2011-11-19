(ns quadkeys.core
  (use [clojure.string])
  (use [clojure.java.io])  
  (require [quadkeys.quadkeys]))

;;ARGV[0] => path/to/coordinates.tsv
(defn -main [& args]
  (when-not (empty? args)
      (with-open [rdr (reader (first args))]
        (doseq [line (line-seq rdr)]
          (let [coords (map #(Double/parseDouble %) (split line #"\t"))]
            (println (join "\t" [line (lat-lng-to-quadkey (double (first coords)) (double (second coords)) 11)])))))
      (recur (rest args))))
