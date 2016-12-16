(ns alt-params-middleware.core-test
  (:require [alt-params-middleware.core :as my]
            [clojure.test :refer :all]
            [ring.middleware.params :refer :all]
            [ring.util.io :refer [string-input-stream]])
  (:import org.apache.commons.codec.net.URLCodec))

;; Browsers will url encode a Shift_JIS "モジバケコワイ" like this
;;proof=> http://d.hatena.ne.jp/keywordsearchmobile?word=%83%82%83W%83o%83P%83R%83%8F%83C
(def url-encoded-mojibake-kowai-str "%83%82%83W%83o%83P%83R%83%8F%83C")
(def test-request {:query-string (str "foo=" url-encoded-mojibake-kowai-str)})

(def encoded-mojibake-kowai-str "%83%82%83W%83o%83P%83R%83%8F%83C")
(def correcly-decoded-mojibake-kowai-str "モジバケコワイ")
(def wrongly-decoded-mojibake-kowai-str "モ�W�o�P�Rワ�C")

(deftest decode-url-encoded-shift-jis
  (is (= (my/commons-decoder url-encoded-mojibake-kowai-str "Shift_JIS")
         "モジバケコワイ")))

(def wrapped-echo (wrap-params identity))

(deftest decoding-of-ring-params-default
  (is (= (:params (wrapped-echo test-request))
         {"foo" "���W�o�P�R���C"})))

(def ring-shift-jis-echo (wrap-params identity {:encoding "Shift_JIS"}))

(deftest decoding-of-ring-params-shift-jis
  (is (= (:params (ring-shift-jis-echo test-request))
         {"foo" "モ�W�o�P�Rワ�C"})))

(def my-shift-jis-echo (my/wrap-params identity {:encoding "Shift_JIS"
                                                 :decoder my/commons-decoder}))

(deftest decoding-with-provided-decoder
  (is (= (:params (my-shift-jis-echo test-request))
         {"foo" "モジバケコワイ"})))

;;Without the decoder option, it should behave the same as the ring one.
(def my-shift-jis-echo-without-decoder (my/wrap-params identity {:encoding "Shift_JIS"}))
(def my-default-echo-without-decoder (my/wrap-params identity))
(deftest compatibility-of-default-behavior
  (is (= (my-shift-jis-echo-without-decoder test-request)
         (ring-shift-jis-echo test-request)))
  (is (= (my-default-echo-without-decoder test-request)
         (wrapped-echo test-request))))
