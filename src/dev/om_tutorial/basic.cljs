(ns dev.om-tutorial.basic
  (:require
   [om.next :as om :refer-macros [defui]]
   [sablono.core :refer-macros [html]]
   [devcards.core :as dc :refer-macros [defcard defcard-doc deftest]]))

;; Basic

(defui Hello
  Object
  (render [this]
          (html
           [:p (-> this om/props :text)])))

(def hello (om/factory Hello))

(defcard simple-component-takes-props
  (hello {:text "Hello, world!"}))


;; Counter

(defonce state (atom {:count 0}))


(defui Counter
  static om/IQuery
  (query [this] [:count])
  Object
  (render [this]
          (let [{:keys [count]} (om/props this)]
            (html
             [:div
              [:span (str "Count: " count)] [:br]
              [:button {:on-click
                        (fn [e] (om/transact! this '[(increment)]))}
               "Increment"]]))))

(def reconciler
  (letfn [(read [{:keys [state] :as env} key params]
            (let [st @state]
              (if-let [[_ value] (find st key)]
                {:value value}
                {:value :not-found})))
          (mutate [{:keys [state] :as env} key params]
            (if (= 'increment key)
              {:value [:count]
               :action #(swap! state update-in [:count] inc)}
              {:value :not-found}))
          ]
    (om/reconciler
     {:state state
      :parser (om/parser {:read read :mutate mutate})})))

(defcard component-uses-reconciler
  (dc/dom-node
   (fn [_ node] (om/add-root! reconciler Counter node))))


;; Query Params

(defonce animal-state
  (atom
   {:app/title "Animals"
    :animals/list
    [[1 "Ant"] [2 "Antelope"] [3 "Bird"] [4 "Cat"] [5 "Dog"]
     [6 "Lion"] [7 "Mouse"] [8 "Monkey"] [9 "Snake"] [10 "Zebra"]]}))


(defmulti read (fn [env key params] key))

(defmethod read :default
  [{:keys [state] :as env} key params]
  (let [st @state]
    (if-let [[_ value] (find st key)]
      {:value value}
      {:value :not-found})))

(defmethod read :animals/list
  [{:keys [state] :as env} key {:keys [start end]}]
  {:value (subvec (:animals/list @state) start end)})

(defui AnimalsList
  static om/IQueryParams
  (params [this] {:start 0 :end 10})

  static om/IQuery
  (query [this]
         '[:app/title (:animals/list {:start ?start :end ?end})])
  Object
  (render [this]
          (let [{:keys [app/title animals/list]} (om/props this)]
            (html
             [:div [:h2 title]
              [:ul
               (map
                (fn [[i name]]
                  [:li (str i ". " name)])
                list)]]
             ))))

(def animal-reconciler
  (om/reconciler
   {:state animal-state
    :parser (om/parser {:read read})}))

(defcard component-uses-query-params
  (dc/dom-node
   (fn [_ node] (om/add-root! animal-reconciler AnimalsList node))))

(defcard set-params
  (html
   [:div
    [:button {:on-click
              (fn [_] (om/set-query! 
                       (om/class->any animal-reconciler AnimalsList)
                       {:params {:start 0 :end 5}}))}
     "Short list (5)"]
    [:button {:on-click
              (fn [_] (om/set-query! 
                       (om/class->any animal-reconciler AnimalsList)
                       {:params {:start 0 :end 10}}))}
     "FUll list (10)"]]))
