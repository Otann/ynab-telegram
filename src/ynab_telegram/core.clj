(ns ynab-telegram.core
  (:gen-class)
  (:require [morse.polling :as p]
            [wrench.core :as cfg]
            [morse.handlers :as h]
            [morse.api :as t])
  (:import java.util.Base64))

(cfg/def token {:name "TELEGRAM_TOKEN"})


(defn encode [object-to-encode]
  (.encodeToString (Base64/getEncoder) (json/generate-cbor object-to-encode)))


(defn decode [^String string-to-decode]
  (json/parse-cbor (.decode (Base64/getDecoder) string-to-decode) true))


(def categories
  [{:name       "Indispensable"
    :categories [{:name     "Apartment Rent"
                  :emojicon "\uD83C\uDFE0"}
                 {:name     "Subscriptions"
                  :emojicon "\uD83D\uDCEC"}
                 {:name     "Utilities"
                  :emojicon "\uD83D\uDD0C"}]}
   {:name       "First Priority"
    :categories [{:name     "Transport"
                  :emojicon "\uD83D\uDE8B"}
                 {:name     "Groceries"
                  :emojicon "\uD83D\uDED2"}
                 {:name     "Lunch"
                  :emojicon "\uD83C\uDF2F"}
                 {:name     "Hobbies"
                  :emojicon "\uD83D\uDD79"}]}
   {:name       "Indispensable"
    :categories [{:name     "Going Out"
                  :emojicon "\uD83C\uDF77"}
                 {:name     "Looks"
                  :emojicon "\uD83D\uDC85"}
                 {:name     "Home Improvements"
                  :emojicon "\uD83D\uDECB"}
                 {:name     "Vacation"
                  :emojicon "\uD83D\uDC6B"}]}])


(defn categories-keyboard [categories]
  (for [group categories]
    (for [category (:categories group)]
      {:text          (:emojicon category)
       :callback_data (encode category)})))


(defn category-selected [category]
  [[{:text          (str (:emojicon category) " " (:name category))
     :callback_data (encode {:category-id (:name category)})}]])


(defn random-payee []
  (rand-nth ["Rent For Mar And"
             "Swish \\+46700312902"
             "Ica Supermarket"
             "Pressbyrån"
             "Bolt\\.Eu /O/21013"
             "Amazonretail\\*5i9"
             "Hammarby Sushi &"
             "Wolt"
             "Hellofresh\\*28293"
             "Bauhaus Sickla"]))

(h/defhandler bot-api
  (h/command-fn "start"
    (fn [{{id :id :as chat} :chat}]
      (println "Start: " chat)
      (t/send-text token id "Welcome!")))

  (h/command-fn "reset"
    (fn test-handler [{{id :id :as chat} :chat}]
      (t/send-text token id "Removing a keyboard"
                   {:reply_markup {:remove_keyboard true}})))

  (h/command-fn "keyboard"
    (fn test-handler [{{id :id :as chat} :chat}]
      (t/send-text token id "Setting a keyboard"
                   {:reply_markup
                    {:keyboard
                     [[{:text "🥸 Expenses"}
                       {:text "🥳 Inflows"}]]}})))

  (h/command-fn "transaction"
    (fn test-handler [{{id :id :as chat} :chat}]
      (t/send-text token id
                   (str (if (> (rand-int 10) 8) "🟢" "🔻")
                        " *" (rand-int 1000) " kr* — " (random-payee)
                        "\n2022 January " (rand-int 30))
                   {:parse_mode   "MarkdownV2"
                    :reply_markup {:inline_keyboard (categories-keyboard categories)}})))

  ; So match-all catch-through case would look something like this:
  (h/message-fn (fn [message] (println "Intercepted message:" message)))

  (h/callback-fn (fn [{{message-id    :message_id
                        {chat-id :id} :chat} :message
                       encoded-data          :data
                       :as                   message}]
                   (let [data     (decode encoded-data)
                         keyboard (if (contains? data :category-id)
                                    (categories-keyboard categories)
                                    (category-selected data))]
                     (clojure.pprint/pprint message)
                     (clojure.pprint/pprint data)
                     (t/inline-keyboard-callback token chat-id message-id
                                                 {:inline_keyboard keyboard})))))


(comment
  (def channel (p/start token bot-api))
  (p/stop channel)
  )

(defn -main [& args]
  (when-not (cfg/validate-and-log)
    (System/exit 1))

  (p/start token bot-api))
