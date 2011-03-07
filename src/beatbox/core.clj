(ns beatbox.core
  (:use
   overtone.live
   [org.satta.glob :only [glob]])

  (:require [polynome.core :as poly]
            [space-navigator :as space]))

;;design a sc synth to play the samples
(definst loop-synth [buf 0 vol 1 rate 1 amp 1 wet-dry 0.2 room-size 0  dampening 1]
  (let [src (play-buf 1 buf rate 1.0 0.0 1.0 1)
        rev (free-verb src wet-dry room-size dampening)]
    (* vol amp rev)))

;;change m to point to your monome (use dummy if you don't have one...)
;;(def m (poly/init "/dev/tty.usbserial-m64-0790"))
(def m (poly/init "dummy"))

;;fetch all the samples from the assets dir
(def sample-files (sort (glob "assets/*.{aif,AIF,wav,WAV}")))
(if (empty? sample-files) (throw (Exception. "Can't find any samples in the assets dir.")))

(defn load-samples
  "load samples and return a list of maps containing the loaded sample full path and vol state"
  [files]
  (reduce (fn [sum el]
            (let [path   (.getAbsolutePath el)
                  sample (sload-sample path)]
              (conj sum {:sample sample
                         :path path
                         :state (agent 0)})))
          []
          files))

(defn start-looper
  "Instantiates a new looper synth for a given loaded sample in the sample-map"
  [sample-map]
  (loop-synth (:id (sample-map :sample)) 0))

(def samples (load-samples sample-files))

;;use the samples to create multiple looper synths starting in sync in 500ms time from now
(def loopers
  (at (+ 500 (now))
      (zipmap (poly/coords m)
              (map #(assoc % :synth (start-looper %)) samples))))

(defn toggle
  "Invert the vol from 1 to 0 or 0 to 1"
  [vol]
  (mod (inc vol) 2))

(defn silence
  "Silence the vol (always return 0)"
  [vol]
  0)

(defn change-vol
  "Update vol with update-fn and change led state and loop vol accordingly"
  [vol update-fn looper x y]
  (let [new-vol (update-fn vol)
        synth   (looper :synth)
        path    (looper :path)]
    (ctl synth :vol new-vol)
    (poly/led m x y)
    (if (= 1 new-vol)
      (println "Playing " path)
      (println "Stopping" path))
    new-vol))

(defn trigger
  "Invert the volume for the loop corresponding to the given x y coords. Also
   update the associated agent's state and monome LED state."
  [x y]
  (let [looper (get loopers [x y])
        state (looper :state)]
    (send state change-vol toggle looper x y)))

(defn mute
  "Silences all loopers."
  []
  (doseq [[coords looper] loopers] (apply send (looper :state) change-vol silence looper coords )))

;;(trigger 1 7)
;;(mute)

(comment
  ;;play about with synth params:
  (def rate (atom 1))
  (ctl loop-synth :rate 1)
  (ctl loop-synth :amp 0.1)
  (ctl loop-synth :room-size 1)
  (ctl loop-synth :dampening 1)
  (ctl loop-synth :wet-dry 0.1)

  (defn tempo-slide [to]
    (let [from @rate
          step (if (< from to) 0.01 -0.01)
          vals (range from to step)]
      (doall (map #(do (ctl loop-synth :rate (reset! rate %)) (Thread/sleep 35)) vals))))

  (ctl loop-synth :rate 1)

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
                        space/SAMPLE-RANGES)))
