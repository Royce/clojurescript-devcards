(ns dev.validation
  (:require
   [clojure.string]
   [vlad.core :as vlad :refer [Validation
                               chain join
                               attr equals-field
                               present matches predicate length-in
                               validate valid?
                               guess-field-names
                               translate-errors english-translation]]
   [cljs.test])
  (:require-macros
   [cljs.test :refer [is testing]]
   [devcards.core :as dc :refer [defcard defcard-doc deftest]]))

(enable-console-print!)

(def invalid? (comp not valid?))
(def validate-and-translate
  (comp #(translate-errors % english-translation)
             guess-field-names
             validate))


(def data {:new-user {:name "Jack"
                      :preferred-name ""
                      :age "23"
                      :password "12qwasZX"
                      :confirmation "12qwasZX"}})
(defcard data data)


(defmethod english-translation :dev.validation/contains
  [{:keys [name pattern]}]
  (str name " must contain the pattern " (.toString pattern) "."))

(defn contains
  ([pattern]
   (contains pattern {}))
  ([pattern error-data]
   (predicate #(nil? (re-seq pattern %))
             (merge {:type ::contains :pattern pattern} error-data))))


(defrecord Condition [condition validation]
  Validation
  (validate [{:keys [condition validation]} data]
      (if (condition data)
        (validate validation data)
        [])))

(defn condition [cond validation]
  (if (fn? cond)
    (Condition. cond validation)
    (Condition. #(vlad/valid? cond %) validation)))




;; Password

(def password
  (chain (attr [:password]
               (chain (present)
                      (join (length-in 6 128)
                            (contains #"[a-z]")
                            (contains #"[A-Z]")
                            (contains #"[0-9]"))))
         (equals-field [:password] [:confirmation])))
(defcard-doc "## Password"
  (dc/mkdn-pprint-source password))
(defcard password
  (dc/tests
   (is (valid? (attr [:new-user] password) data))))


;; Numeric


(defmethod english-translation :dev.validation/numeric-string
  [{:keys [name pattern]}]
  (str name " must be a number."))

(defn numeric-string
  ([]
   (numeric-string {}))
  ([error-data]
   (predicate #(or (not (string? %))
                   (nil? (re-matches #"[0-9]+" %)))
              (merge {:type ::numeric-string} error-data))))

(defcard-doc "## Numeric" (dc/mkdn-pprint-source numeric-string))
(defcard numeric
  (dc/tests
   (is (valid? (attr [:new-user :age] (numeric-string)) data))
   (is (invalid? (numeric-string) ""))
   (is (invalid? (numeric-string) nil))
   (is (valid? (numeric-string) "0"))
   (is (valid? (numeric-string) "23"))
   (is (invalid? (numeric-string) "a"))
   ))


; Phone numbers

(def phone-number-mask (matches #"([0-9+ \(\)])*"))
(defcard-doc "## Phone numbers" (dc/mkdn-pprint-source phone-number-mask))

(defcard phone-number
  (dc/tests
   (is (valid? phone-number-mask ""))
   (is (valid? phone-number-mask "+614"))
   (is (valid? phone-number-mask "0405 018 016"))
   (is (valid? phone-number-mask "(08) 0501 8016"))
   (is (invalid? phone-number-mask "+A"))
   ))


;; Required

(def new-user-required
  (join (attr [:name] (present))
        (attr [:age] (condition (present) (numeric-string)))
        password))

(defcard-doc "## Required" (dc/mkdn-pprint-source new-user-required))
(defcard required
  (dc/tests
   (is (valid? (attr [:new-user] new-user-required) data))
   (is (valid? (attr [:new-user] new-user-required) (assoc-in data [:new-user :age] nil)))
   ))

(defcard (validate new-user-required {}))

(defcard (validate-and-translate
          (attr [:form] password) {:form {:password "1adswe"}}))
