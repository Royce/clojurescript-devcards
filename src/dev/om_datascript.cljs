(ns dev.om-datascript
  (:require
   [om.next :as om :refer-macros [defui]]
   [datascript.core :as d]
   [sablono.core :refer-macros [html]]
   [devcards.core :as dc :refer-macros [defcard defcard-doc deftest]]))

(enable-console-print!)


(def conn (d/create-conn
           {:key {:db/unique :db.unique/identity}
            :category/name {:db/unique :db.unique/identity}
            :thing/name {:db/unique :db.unique/identity}
            :thing/url {}
            :thing/type {:db/cardinality :db.cardinality/many
                         :db/valueType   :db.type/ref}
            }))

(d/transact! conn
  [{:db/id -1
    :meta :meta
    :app/title "Hello, DataScript!"
    :app/version 0
    :counter 0}])


(defmulti read om/dispatch)

(defmethod read :meta
  [{:keys [state selector]} _ _]
  {:value (d/q '[:find (pull ?e ?selector) .
                 :in $ ?selector
                 :where [?e :meta]]
            (d/db state) selector)})

;; (defcard r ((om/parser {:read read})
;;             {:state conn}
;;             [{:meta [:db/id :app/title :app/version :counter]}]))


(defmulti mutate om/dispatch)

(defmethod mutate 'app/increment
  [{:keys [state]} _ entity]
  {:value [:counter]
   :action (fn [] (d/transact! state
                    [(update-in entity [:counter] inc)]))})


(defui Counter
  static om/IQuery
  (query [this] [{:meta [:db/id :app/title :app/version :counter]}])
  Object
  (render [this]
          (let [{:keys [app/title app/version counter] :as entity}
                ((om/props this) :meta)]
            (html
             [:div
              [:h3 (str title " ver:" version)]
              [:span (str "Count: " counter)] [:br]
              [:button {:on-click
                        (fn [e] (om/transact! this
                                              `[(app/increment ~entity)]))}
               "Increment"]]))))

(def reconciler
  (om/reconciler
   {:state conn
    :parser (om/parser {:read read :mutate mutate})}))

(defcard counter
  (dc/dom-node
   (fn [_ node] (om/add-root! reconciler Counter node))))


;; (defcard-doc (dc/mkdn-pprint-source om/IQuery))




;; Schema
;; :category/name {:db/unique :db.unique/identity}
;; :key {:db/unique :db.unique/identity}
(d/transact!
 conn
 [{:category/name "Stuff" :key :stuff}
  {:category/name "Things" :key :things}
  {:category/name "Blah" :key :blah}
])

(defn id-for-key [conn key]
  (let [query '[:find (pull ?e [:db/id]) .
                :in $ ?key
                :where [?e :key ?key]]]
    ((d/q query (d/db conn) key) :db/id)))

(def stuff  (id-for-key conn :stuff))
(def things (id-for-key conn :things))
(def blah   (id-for-key conn :blah))


;; Schema
;; {:thing/name {:db/unique :db.unique/identity}
;;  :thing/url  {}
;;  :thing/type {:db/cardinality :db.cardinality/many
;;               :db/valueType   :db.type/ref}}

(d/transact!
 conn
 [{:thing/name "Hat" :thing/url "hat.com" :thing/type [stuff things]}
  {:thing/name "Cat" :thing/url "cat.com" :thing/type [blah things]}
  {:thing/name "Fez" :thing/url "fez.com" :thing/type [stuff blah]}
  {:thing/name "Mat" :thing/url "mat.com" :thing/type [blah]}
  {:thing/name "Dog" :thing/url "dog.com" :thing/type [things]}])

(defmethod read :things
  [{:keys [state selector]} _ _]
  {:value (d/q '[:find [(pull ?e ?selector) ...]
                 :in $ ?selector [?type ...]
                 :where [?e :thing/type ?type
                         ]]
            (d/db state) selector [blah]
               )})

;; (defcard r ((om/parser {:read read :mutate :mutate})
;;             {:state conn}
;;             [{:things [:db/id
;;                        :thing/name
;;                        :thing/url
;;                        {:thing/type [:db/id :category/name]}]}]))

(defn categories->str [cats]
  (clojure.string/join ", " (map :category/name cats)))

(defui Table
  static om/IQuery
  (query [this] [{:things [:db/id :thing/name :thing/url
                           {:thing/type [:db/id :category/name]}]}])
  Object
  (render [this]
          (html
           [:table
            [:thead [:tr [:th "Name"] [:th "Url"] [:th "Type"]]]
            [:tbody
             (map (fn [{:keys [thing/name thing/url thing/type] :as entity}]
                    [:tr
                     [:td name]
                     [:td url]
                     [:td (categories->str type)]])
                  ((om/props this) :things))]])))

(defcard table
  (dc/dom-node
   (fn [_ node] (om/add-root! reconciler Table node))))

