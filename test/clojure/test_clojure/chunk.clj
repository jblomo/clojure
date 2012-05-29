;   Copyright (c) Rich Hickey. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

; Author: Jim Blomo


(ns clojure.test-clojure.chunk
  (:use clojure.test))

(deftest chunks-can-seq
  (let [ov [1 2 3]
        pv (vector-of :int 1 2 3)]

    (is (= '(1 2 3) (seq (chunk-first (seq ov))))
        "Object vector chunks are seqable")

    (is (= '(1 2 3) (seq (chunk-first (seq pv))))
        "Primitive vector chunks are seqable")))

