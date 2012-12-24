(ns defalt
  (require [clojure.string :as string]))

;; symbol key -> :selected
;; symbol key -> :alternatives -> alternative key -> alternative function
(def registry (atom {}))

(defn parse-alt-name [alt-name]
  (let [index (.lastIndexOf alt-name ":")]
    (if (= index -1)
      (throw (Exception. (str "defalt name \"" alt-name "\" does not contain a ':'"))))
    (map #(symbol (apply str %))
         [(take index alt-name)
      (drop (inc index) alt-name)])))

(defmacro defalt
  [combo-name params & impl]
  (let [[master-name alt-name] (parse-alt-name (name combo-name))
        fun `(fn ~(vec params) ~@impl)]
    (if-not (get registry master-name)
      (do
       (swap! registry assoc-in
              [*ns* master-name :selected] ::master)
       (swap! registry assoc-in
              [*ns* master-name :alternatives ::master] @(resolve master-name))))
    (swap! registry assoc-in
           [*ns* master-name :alternatives alt-name]
           (with-meta
             (eval fun)
             {:source `'(~'defalt ~combo-name ~params ~@impl)}))
    nil))

(defmacro switch [original alternative]
  (let [alt (get-in @registry [*ns* original :alternatives alternative])]
    (if-not alt
      (throw
       (Exception.
        (str "No alternative \"" alternative "\" for function " original))))
    (swap! registry assoc-in
           [original :selected] alternative)
    (alter-var-root
     (resolve original)
     (fn [_] alt)))
  nil)

(defmacro reset [original]
  `(switch ~original ::master))

#_(defmacro source [original alternative]
    (:source
     (meta
      (get-in @registry [original :alternatives alternative]))))

(defn show*
  ([original] (show* *ns* original))
  ([ns original]
     (let [enabled (get-in @registry
                           [ns original :selected])
           names (remove #{::master}
                         (keys (get-in @registry
                                       [ns original :alternatives])))
           names (map (fn [x index] {:name x :index index :enabled? (= x enabled)})
                      names (iterate inc 1))]
       (println (str "Alternatives for " original ":"))
       (println (str (if (= enabled ::master) "* " "  ") "0. <<master>>"))
       (doseq [alt names]
         (println (str (if (alt :enabled?) "* " "  ")
                       (alt :index) ". "
                       (alt :name)))))))

(defmacro show [ns & [original]]
  (if original
    (show* ns original)
    (show* ns)))

(defn show-ns* [& [ns]]
  (let [ns (or ns *ns*)]
    (println "Showing defalt info for namespace" (.getName ns))
    (doseq [fun (sort-by name (keys (@registry ns)))]
      (show* ns fun))))

(defmacro show-ns [& [ns]]
  (if ns
    (show-ns* ns)
    (show-ns*)))

(defmacro show-global []
  `(doseq [ns# (sort-by #(.getName %) (keys @registry))]
     (show-ns* ns#)
     (println)))

;;(defn add-numbers [a b] (+ a b))
;;(defalt add-numbers:memo [a b] 6)
;;(defalt add-numbers:mock [a b] 0)
