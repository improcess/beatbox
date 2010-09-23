(ns beatbox.core
  (:use
   overtone.live
   [org.satta.glob :only [glob]])

  (:require [polynome.core :as poly]
            [overtone-contrib.core :as contrib]))

(contrib/boot-and-wait)

(def m (poly/init "/dev/tty.usbserial-m64-0790"))

(def sample-files (glob "assets/*.{aif,AIF,wav,WAV}"))

(defn file->name:sample
  [file]
  (let [path (.getAbsolutePath file)]
      (vec (list path
                 (load-sample path)))))

(def sample-bufs (into {} (map file->name:sample sample-files)))

(defsynth looper [buf 0 vol 1]
  (* vol
     (overtone.ugens/play-buf 1 buf 1.0 1.0 0.0 1.0 1)))


(def loops
  (at (+ 1000 (System/currentTimeMillis))
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

(poly/on-press m (fn [x y] (trigger x y)))
