;; David Snyder Oct 2011
;; Functions to convert WGS-84 Latitude/Longitude pairs into Quadkeys at a zoom
;; level in 1-23
;; Adapted from the discussion at http://msdn.microsoft.com/en-us/library/bb259689.aspx

(defn deg-to-rad [deg] (/ (* Math/PI deg) 180))

(defn rad-to-deg [rad] (/ (* 180 rad) Math/PI))

;; private static double Clip(double n, double minValue, double maxValue)
;; {
;;  return Math.Min(Math.Max(n, minValue), maxValue);
;;  }

(defn clamp [n,min_n,max_n] (min (max n min_n) max_n))

;; public static uint MapSize(int levelOfDetail)
;;      {
;;          return (uint) 256 << levelOfDetail;
;;      }

(defn map-size [lvl] (bit-shift-left 256 lvl))

;; public static void LatLongToPixelXY(double latitude, double longitude, int levelOfDetail, out int pixelX, out int pixelY)
;;     {
;;         latitude = Clip(latitude, MinLatitude, MaxLatitude);
;;         longitude = Clip(longitude, MinLongitude, MaxLongitude);
;; 
;;         double x = (longitude + 180) / 360; 
;;         double sinLatitude = Math.Sin(latitude * Math.PI / 180);
;;         double y = 0.5 - Math.Log((1 + sinLatitude) / (1 - sinLatitude)) / (4 * Math.PI);
;; 
;;         uint mapSize = MapSize(levelOfDetail);
;;         pixelX = (int) Clip(x * mapSize + 0.5, 0, mapSize - 1);
;;         pixelY = (int) Clip(y * mapSize + 0.5, 0, mapSize - 1);
;;     }

(defn lat-lng-to-pixels [lat,lng,lvl]
  "Returns the pixel coordinates for the given WGS-84 latitude,longitude at the given zoom level from 1-23"
  (let [lat (clamp lat -85 85)
        lng (clamp lng -180 180)
        sin-lat (Math/sin (deg-to-rad lat))
        size    (map-size lvl)]
    [(int (clamp (+ 0.5 (* size (/ (+ lng 180) 360))) 0 (- size 1)))
     (int (clamp (+ 0.5 (* size (- 0.5 (/ (Math/log (/ (+ 1 sin-lat) (- 1 sin-lat))) (* Math/PI 4))))) 0 (- size 1)))
     ]
    ))

;; public static void PixelXYToTileXY(int pixelX, int pixelY, out int tileX, out int tileY)
;;       {
;;           tileX = pixelX / 256;
;;           tileY = pixelY / 256;
;;       }

(defn pixels-to-tile [x,y] [(/ x 256) (/ y 256)])

;; public static string TileXYToQuadKey(int tileX, int tileY, int levelOfDetail)
;;         {
;;             StringBuilder quadKey = new StringBuilder();
;;             for (int i = levelOfDetail; i > 0; i--)
;;             {
;;                 char digit = '0';
;;                 int mask = 1 << (i - 1);
;;                 if ((tileX & mask) != 0)
;;                 {
;;                     digit++;
;;                 }
;;                 if ((tileY & mask) != 0)
;;                 {
;;                     digit++;
;;                     digit++;
;;                 }
;;                 quadKey.Append(digit);
;;             }
;;             return quadKey.ToString();

(defn quad-for-lvl [x,y,lvl]
  "Returns a single quadkey in range 0-3 for the given zoom level from 1-23"
  (let [mask (bit-shift-left 1 (- lvl 1))]
    (+ (if (not (= 0 (bit-and y mask))) 2 0) (if (not (= 0 (bit-and x mask))) 1 0))))

 (defn tile-to-quadkey [x,y,lvl]
   "Returns the quadkey of length lvl corresponding to the given tile coordinates"
   (if (= 0 lvl) nil (str (quad-for-lvl x y lvl) (tile-to-quadkey x y (- lvl 1)))))

(defn lat-lng-to-quadkey [x,y,lvl]
  "Returns the quadkey for a given WGS-84 latitude,longitude and zoom level from 1-23"
  (let [pixelxy (lat-lng-to-pixels x y lvl)
        tilexy (pixels-to-tile (first pixelxy) (last pixelxy))]
    (tile-to-quadkey (int (first tilexy)) (int (last tilexy)) lvl)))
