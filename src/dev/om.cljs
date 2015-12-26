(ns dev.om
  (:require
   [om.next :as om :refer-macros [defui]]
   [sablono.core :refer-macros [html]]
   [devcards.core :as dc :refer-macros [defcard defcard-doc deftest]]))

(enable-console-print!)

;; Basic

(defui Hello
  Object
  (render [this]
          (html
           [:p (-> this om/props :text)])))

(def hello (om/factory Hello))

(defcard simple-component
  (hello {:text "Hello, world!"}))


;; Counter

(defonce state (atom {:count 0}))

(defn read [{:keys [state] :as env} key params]
  (let [st @state]
    (if-let [[_ value] (find st key)]
      {:value value}
      {:value :not-found})))

(defn mutate [{:keys [state] :as env} key params]
  (if (= 'increment key)
    {:value [:count]
     :action #(swap! state update-in [:count] inc)}
    {:value :not-found}))

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
  (om/reconciler
   {:state state
    :parser (om/parser {:read read :mutate mutate})}))

(defcard counter
  (dc/dom-node
   (fn [_ node] (om/add-root! reconciler Counter node))))
(defcard counter-state state)


;; (defcard-doc (dc/mkdn-pprint-source om/IQuery))
