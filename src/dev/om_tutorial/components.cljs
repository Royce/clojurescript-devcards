(ns dev.om-tutorial.components
  (:require
   [om.next :as om :refer-macros [defui]]
   [sablono.core :refer-macros [html]]
   [devcards.core :as dc :refer-macros [defcard defcard-doc deftest]]))

(def init-data
  {:list/one [;{:name "John" :points 0}
              {:name "Mary" :points 0}
              {:name "Bob"  :points 0}]
   :list/two [{:name "Mary" :points 0 :age 27}
              ;{:name "Gwen" :points 0}
              {:name "Jeff" :points 0}]})
(defcard data init-data)

(defmulti read om/dispatch)

(defn get-people [state key]
  (let [st @state]
    (into [] (map #(get-in st %)) (get st key))))

(defmethod read :list/one
  [{:keys [state] :as env} key params]
  {:value (get-people state key)})

(defmethod read :list/two
  [{:keys [state] :as env} key params]
  {:value (get-people state key)})

(defmethod read :default
  [{:keys [state] :as env} key params]
  (let [st @state]
    (if-let [[_ value] (find st key)]
      {:value value}
      {:value :not-found})))

(defui Person-Simple
  static om/Ident
  (ident [this {:keys [name]}]
         [:person/by-name name])
  static om/IQuery
  (query [this]
         '[:name :points])
  )

(defui RootView-Simple
  static om/IQuery
  (query [this]
         (let [subquery (om/get-query Person-Simple)]
           `[{:list/one ~subquery} {:list/two ~subquery}])))

(def norm-data (om/tree->db RootView-Simple init-data true))
(defcard normalized norm-data)
(defcard reconstruct ((om/parser {:read read})
                      {:state (atom norm-data)}
                      '[:list/one]))

;; Mutation

(defmulti mutate om/dispatch)

(defmethod mutate 'points/inc
  [{:keys [state] :as env} _key {:keys [name]}]
  {:action
   (fn [] (swap! state update-in [:person/by-name name :points] inc))
   })

(defmethod mutate 'points/dec
  [{:keys [state] :as env} _key {:keys [name]}]
  {:action
   (fn [] (swap! state update-in [:person/by-name name :points] dec))
   })

(defcard mutate-and-reconstruct
  (let [parser (om/parser {:read read :mutate mutate})
        st (atom norm-data)]
    (parser {:state st} '[(points/inc {:name "Mary"})])
    (parser {:state st} '[(points/dec {:name "Jeff"})])
    (parser {:state st} '[:list/one :list/two])
    ))

;; View / Nesting

(defui Person
  static om/Ident
  (ident [this {:keys [name]}]
         [:person/by-name name])
  static om/IQuery
  (query [this]
         '[:name :points :age])
  Object
  (render [this]
          (println "Render Person" (-> this om/props :name))
          (let [{:keys [points name] :as props} (om/props this)]
            (html
             [:li
              [:label (str name ", points: " points)]
              [:button
               {:on-click (fn [_] (om/transact! this `[(points/inc ~props)]))}
               "+"]
              [:button
               {:on-click (fn [_] (om/transact! this `[(points/dec ~props)]))}
               "-"]
              ]
             ))))

(def person (om/factory Person {:keyfn :name}))

(defui ListView
  Object
  (render [this]
          (println "Render ListView" (-> this om/path first))
          (let [list (om/props this)]
            (html [:ul (map person list)])
            )))

(def list-view (om/factory ListView))

(defui AllListsView
  static om/IQuery
  (query [this]
         (let [subquery (om/get-query Person)]
           `[{:list/one ~subquery} {:list/two ~subquery}]))
  Object
  (render [this]
          (println "Render All-List Vview")
          (let [{:keys [list/one list/two]} (om/props this)]
            (html [:div
                   [:h2 "List A"]
                   (list-view one)
                   [:h2 "List B"]
                   (list-view two)])
            )))

(def reconciler
  (om/reconciler
   {:state  init-data
    :parser (om/parser {:read read :mutate mutate})}))

(defcard everything
  (dc/dom-node
   (fn [_ node] (om/add-root! reconciler AllListsView node))))

(defcard person-query (om/get-query Person))
(defcard root-query (om/get-query AllListsView))

