(ns quadkeys.test.core
  (:use [quadkeys.core])
  (:use [clojure.test]))

(deftest pixels-to-tile-256
  (is (= (pixels-to-tile 256 256) [1 1])))

(deftest lat-lng-to-pixels-conversions
  (is (= (lat-lng-to-pixels 30.297463 -97.757506 5) [1871,3372]))
  (is (= (lat-lng-to-pixels 30.297463 -97.757506 11) [119774, 215807])))

(deftest quad-for-lvl-3
  (is (= 2 (quad-for-lvl 3 5 3))))

(deftest tile-to-quadkey-conversions
  (is (= "213" (tile-to-quadkey 3 5 3)))
  (is (= "03133233332" (tile-to-quadkey 119774, 215807,11))))

(deftest lat-lng-to-quadkey-11
  (is (= "02313012031" (lat-lng-to-quadkey 30.297463 -97.757506 11))))
