(ns dev.om-datascript
  (:require
   [om.next :as om :refer-macros [defui]]
   [datascript.core :as d]
   [sablono.core :refer-macros [html]]
   [devcards.core :as dc :refer-macros [defcard defcard-doc deftest]]))

(enable-console-print!)


(def conn (d/create-conn {}))

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