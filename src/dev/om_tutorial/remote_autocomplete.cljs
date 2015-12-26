(ns dev.om-tutorial.remote_autocomplete
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [om.next :as om :refer-macros [defui]]
   [om.next.impl.parser]
   [cljs.core.async :as async :refer [<! >! put! chan]]
   [clojure.string :as string]
   [sablono.core :refer-macros [html]]
   [devcards.core :as dc :refer-macros [defcard defcard-doc deftest]])
  (:import [goog Uri]
           [goog.net Jsonp])
  )

(enable-console-print!)

(def base-url
  "http://en.wikipedia.org/w/api.php?action=opensearch&format=json&search=")

(defn jsonp
  ([uri] (jsonp (chan) uri))
  ([c uri]
   (let [gjsonp (Jsonp. (Uri. uri))]
     (.send gjsonp nil #(put! c %))
     c)))


(defmulti read om/dispatch)

(defmethod read :search/results
  [{:keys [state ast] :as env} k {:keys [query]}]
  (do
    (println "read: " query)
    (merge
    {:value (get @state k [])}
    (when-not (or (string/blank? query)
                  (< (count query) 3))
      {:search ast}))))


;; UI

(defn result-list [results]
  [:ul {:key "result-list"}
   (map (partial conj [:li]) results)])

(defn search-field [ac query]
  [:input
   {:key "search-field"
    :value query
    :on-change
    (fn [e]
      (om/set-query! ac {:params {:query (.. e -target -value)}}))}])

(defui AutoCompleter
  static om/IQueryParams
  (params [_]
          {:query ""})
  static om/IQuery
  (query [_]
         '[(:search/results {:query ?query})])
  Object
  (render [this]
          (let [{:keys [search/results]} (om/props this)]
            (html [:div
                   [:h2 "Autocompleter"]
                   (cond-> [(search-field this (:query (om/get-params this)))]
                     (not (empty? results)) (conj (result-list results)))]))))

(defn search-loop [c]
  (go
    (loop [[query cb] (<! c)]
      (let [[_ results] (<! (jsonp (str base-url query)))]
        (cb {:search/results results}))
      (recur (<! c)))))

(defn send-to-chan [c]
  (fn [{:keys [search]} cb]
    (do
      (println search)
      (when search
       (let [{[search] :children} (om.next.impl.parser/query->ast search)
             query (get-in search [:params :query])]
         (put! c [query cb]))))))

(defonce send-chan (chan))

(def reconciler
  (om/reconciler
   {:state {:search/results []}
    :parser (om/parser {:read read})
    :send (send-to-chan send-chan)
    :remotes [:remote :search]}))

(search-loop send-chan)

(defcard ui
  (dc/dom-node
   (fn [_ node] (om/add-root! reconciler AutoCompleter node))))

