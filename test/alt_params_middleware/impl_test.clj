(ns alt-params-middleware.impl-test
  (:require [alt-params-middleware.core :refer :all]
            [clojure.test :refer :all])
  (:import org.apache.commons.codec.net.URLCodec))

(def encoded-mojibake-kowai-str "%83%82%83W%83o%83P%83R%83%8F%83C")
(def correcly-decoded-mojibake-kowai-str "モジバケコワイ")
(def wrongly-decoded-mojibake-kowai-str "モ�W�o�P�Rワ�C")

(deftest form-decode-str-test
  (is (= wrongly-decoded-mojibake-kowai-str
         (form-decode-str encoded-mojibake-kowai-str
                          "Shift_JIS")))
  (is (= correcly-decoded-mojibake-kowai-str
         (form-decode-str encoded-mojibake-kowai-str
                          "Shift_JIS" commons-decoder ))))
