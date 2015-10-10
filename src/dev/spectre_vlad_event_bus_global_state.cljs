(ns dev.spectre_vlad_event_bus_global_state
  (:require
   [com.rpl.specter :as s :refer [select setval ALL]]
   [rum.core :as rum]
   [cljs.core.async :as async]
   [clojure.string]
   [vlad.core :as vlad]
   [dev.validation :refer [condition numeric-string phone-number-mask]]
   )
  (:require-macros
   [cljs.core.async.macros :refer [go-loop]]
   [devcards.core :as dc :refer [defcard]]))

(enable-console-print!)


(def masks {[:form :age] (numeric-string)
            [:form :phone] phone-number-mask})

(defn setval- [state path value masks]
  (let [mask (condition (vlad/present) (masks path))
        mask-validation (vlad/validate (or mask vlad/valid) value)
        state (if (empty? mask-validation)
                (-> state
                    (->> (setval path value))
                    (dissoc :_warning))
                (setval (vec (concat [:_warning] path))
                        (map vlad.core/english-translation mask-validation)
                        state))]
    state))

(defcard setval (-> {:form {:age "12" :phone "95359513"}}
                       (setval- [:form :age] "" masks)
                       (setval- [:form :age] "a" masks)
                       (setval- [:form :age] "23" masks)
                       ))

(defn new-state [state [event path data] {:keys [masks]}]
  (case event
    :setval (setval- state path data masks)))

(defonce state (atom {:heading "The heading"
                      :form {:name "Jack"
                             :age ""
                             :phone ""}
                      }))

(defonce bus
  (let [ch (async/chan)]
    (go-loop []
             (let [msg (async/<! ch)]
               (println msg)
               (swap! state new-state msg {:masks masks})
               (swap! state assoc :_render (js/Math.random)))
             (recur))
    ch))


(rum/defc text < rum/static [state path bus {:keys [label]}]
  (let [id (clojure.string/join path)
        error   (first (select path (state :_error)))
        warning (first (select path (state :_warning)))]
    [:div
     [:label {:for id} label]
     [:input {:id id
              :value (select path state)
              :on-change
              (fn [e]
                (.preventDefault e)
                (async/put! bus [:setval path (.. e -target -value)]))}]
     (if error error (if warning warning))]))

(rum/defc ui < rum/static [state bus]
  [:div
   [:h1 (state :heading)]
   (text state [:form :name] bus {:label "Name"})
   (text state [:form :age]  bus {:label "Age"})
   (text state [:form :phone]  bus {:label "Phone"})])

(defcard ui (fn [s _] (ui @s bus)) state)


(defcard state state)
