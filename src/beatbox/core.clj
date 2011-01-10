(ns beatbox.core
  (:use
   overtone.live
   [org.satta.glob :only [glob]])

  (:require [polynome.core :as poly]
            [space-navigator :as space]))

(def m (poly/init "/dev/tty.usbserial-m64-0790"))
(def sample-files (glob "assets/*.{aif,AIF,wav,WAV}"))

(defn file->path:loaded-sample
  [file]
  (let [path (.getAbsolutePath file)]
      (vec (list path
                 (load-sample path)))))

(def sample-bufs (into {} (map file->path:loaded-sample sample-files)))

(definst looper [buf 0 vol 1 rate 1]
  (* vol
     (play-buf 1 buf rate 1.0 0.0 1.0 1)))

(Thread/sleep 4000)

(def loops
  (at (+ 2000 (System/currentTimeMillis))
      (zipmap (keys sample-bufs) (map #(looper % 0) (vals sample-bufs)))))

(def state (zipmap (poly/coords m) (repeatedly #(agent 1))))

(defn find-loop
  [x y]
  (let [names (vec (apply list (keys loops)))
        name  (get names (poly/button-id m x y) (first loops))]
    (get loops name)))

(defn toggle-loop
  [vol]
  (clojure.core/mod (inc vol) 2))


(defn trigger
  [x y]
  (let [ag (get state [x y])
        state @ag
        loop (find-loop x y)]
    (send ag toggle-loop)
    (snd "/n_set" loop "vol" state)
    (poly/led m x y state)
    (println "/n_set" x y loop state)))

(poly/on-press m (fn [x y s] (trigger x y)))

(comment
  (def s (space/space-navigator))

  (space/on-diff-vals s
                      (fn [vals]
                        (ctl looper :rate (:x vals)))
                      {:min-x 0
                       :max-x 5}
                      space/SAMPLE-RANGES))

