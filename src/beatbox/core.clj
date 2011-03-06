(ns beatbox.core
  (:use
   overtone.live
   [org.satta.glob :only [glob]])

  (:require [polynome.core :as poly]
            [space-navigator :as space]))

(def sample-files (sort (glob "assets/*.{aif,AIF,wav,WAV}")))
(if (empty? sample-files) (throw (Exception. "Can't find any samples in the assets dir.")))

(definst looper [buf 0 vol 1 rate 1 amp 1 wet-dry 0.2 room-size 0  dampening 1]
  (let [src (play-buf 1 buf rate 1.0 0.0 1.0 1)
        rev (free-verb src wet-dry room-size dampening)]
    (* vol amp rev)))

;;(def m (poly/init "/dev/tty.usbserial-m64-0790"))
(def m (poly/init "dummy"))

(defn file->path:loaded-sample
  "Converts a file to a list containing the full path and the loaded sample info"
  [file]
  (let [path (.getAbsolutePath file)]
    (vec (list path
               (sload-sample path)))))

(def sample-bufs (into {} (map file->path:loaded-sample sample-files)))

(def loops
  (at (+ 1000 (System/currentTimeMillis))
      (zipmap (keys sample-bufs) (map #(looper % 0) (vals sample-bufs)))))

(def state (zipmap (poly/coords m) (repeatedly #(agent 0))))

(defn find-loop
  "Finds a particular loop within loops for a given x y pair"
  [x y]
  (let [names (vec (apply list (keys loops)))
        button-id (poly/button-id m x y)
        default (first loops)
        name  (get names button-id default)]
    (println "Current loop:" name)
    (get loops name)))

(defn toggle-loop
  "Invert the vol from 1 to 0 or 0 to 1 depending on vol."
  [vol]
  (mod (inc vol) 2))

(defn trigger
  "Invert the volume for the loop corresponding to the given x y coords. Also
   update the associated agent's state and monome LED state."
  [x y]
  (let [ag (get state [x y])
        vol @(send ag toggle-loop)
        loop (find-loop x y)]
    (ctl loop :vol vol)
    (poly/led m x y state)))

(defn mute
  "Silences all running samples. Not thread safe and uses a dirty hack..."
  []
  (let [ags (vals state)]
    (doseq [ag ags] (send ag (fn [_] 1)))
    (doseq [l (vals loops)] (ctl l :vol 0))))

;;play about with synth params:

(comment
  (def rate (atom 1))
(ctl looper :rate 1)
(ctl looper :amp 0.1)
(ctl looper :room-size 1)
(ctl looper :dampening 1)
(ctl looper :wet-dry 0.1)

(defn tempo-slide [to]
  (let [from @rate
        step (if (< from to) 0.01 -0.01)
        vals (range from to step)]
    (doall (map #(do (ctl looper :rate (reset! rate %)) (Thread/sleep 35)) vals))))

(ctl looper :rate 1)

(tempo-slide 1)

(comment
  (def s (space/space-navigator))

  (space/on-diff-vals s
                      (fn [vals]
                        (ctl looper :amp (:rz vals)))
                      {:min-rz 0
                       :max-rz 1}
                      space/SAMPLE-RANGES)

  (space/on-diff-vals s
                      (fn [vals]
                        (ctl looper :wet-dry (:x vals)))
                      {:min-x 0
                       :max-x 1}
                      space/SAMPLE-RANGES)

  (space/on-diff-vals s
                      (fn [vals]
                        (ctl looper :room-size (:z vals)))
                      {:min-z 0
                       :max-z 1}
                      space/SAMPLE-RANGES)

  (space/on-diff-vals s
                      (fn [vals]
                        (ctl looper :dampening (:y vals)))
                      {:min-y 0
                       :max-y 1}
                      space/SAMPLE-RANGES)
  )

)
