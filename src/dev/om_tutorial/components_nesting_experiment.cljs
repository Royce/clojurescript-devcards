(ns dev.om-tutorial.components-nesting-experiment
  (:require
   [om.next :as om :refer-macros [defui]]
   [sablono.core :refer-macros [html]]
   [devcards.core :as dc :refer-macros [defcard defcard-doc deftest]]))

;; Counter

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

(def reconciler-counter
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
     {:state (atom {:count 0})
      :parser (om/parser {:read read :mutate mutate})})))

(defcard component-at-root
  (dc/dom-node
   (fn [_ node] (om/add-root! reconciler-counter Counter node))))


;; Counter nested

(def counter (om/factory Counter))

(defui ContainerView
  Object
  (render [this]
          (let [props (om/props this)]
            (html [:div
                   [:h2 "Container" (:other props)]
                   (counter)])
            )))

(defcard (om/get-query ContainerView))

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
     {:state (atom {:count 0 :other 12})
      :parser (om/parser {:read read :mutate mutate})})))

(defcard component-nested
  (dc/dom-node
   (fn [_ node] (om/add-root! reconciler ContainerView node))))
