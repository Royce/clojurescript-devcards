(ns dev.rum
  (:require
   [rum.core :as rum]
   [cljs.reader]
   [cljs.test])
  (:require-macros
   [cljs.test :refer [is testing]]
   [devcards.core :as dc :refer [defcard defcard-doc deftest]]))

(enable-console-print!)


(defonce an-atom (atom 0))

(rum/defc counter-reactive < rum/reactive []
  [:div
   [:input {:value (rum/react an-atom)}]
   [:button {:on-click #(swap! an-atom inc)} "Increment"]])

(defcard counter-reactive (counter-reactive))

(defcard-doc (dc/mkdn-pprint-source counter-reactive))



(rum/defcs counter-local < (rum/local 0) [rum-state]
  (let [state (:rum/local rum-state)]
    [:div
     [:input {:value @state}]
     [:button {:on-click #(swap! state inc)} "Increment"]]))

(defcard counter-local (counter-local))

(defcard-doc (dc/mkdn-pprint-source counter-local))



(defn str->int [s]
  (when (and (string? s)
        (re-find #"^\d+$" s))
    (cljs.reader/read-string s)))

(def conversions
  {:celcius
   (fn [new] {:celcius new :fahrenheit (+ 32 (* new 1.8))})
   :fahrenheit
   (fn [new] {:celcius (/ (- new 32) 1.8) :fahrenheit new})})

(rum/defc input < rum/cursored [state key label]
  [:div
   [:input {:type "text"
            :value (or (key @state) "")
            :on-change
            (fn [event]
              (let [value (.-value (.-target event))]
                (swap! state assoc key value)))
            :id (name key)}]
   [:label {:for (name key)} label]])

;; < rum/cursored rum/cursored-watch
(rum/defc temperature-conversion < rum/cursored-watch [state]
    [:div
     (input state :fahrenheit "°F")
     " = "
     (input state :celcius "°C")])

(defonce temperatures (atom {:celcius nil :fahrenheit nil}))

(defcard temperature-conversion (temperature-conversion temperatures))

(defcard-doc
  (dc/mkdn-pprint-source str->int))

(deftest temperature-conversion-tests "str->int"
  (testing
    (is (= (str->int "10") 10))
    (is (= (str->int "08") 8))
    (is (= (str->int "x") nil))
    (is (= (str->int " ") nil))
    ))