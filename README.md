# alt-params-middleware

A drop in replacement of the ring's params middleware,
with an option to provide a custom decoder fn.

This is somewhat of a design proposal in responce to the below issues in ring/ring-codec:
- https://github.com/ring-clojure/ring/issues/269
- https://github.com/ring-clojure/ring-codec/pull/15

In a nutshell, ring's params middleware is not capable of decoding some URLencoded strings
from an exotic charset such as Shift_JIS, and this library aims to solve the problem with
an enhanced params middleware that supports a pluggable decoder.
(Currently the decoder used by ring is hard coded to java.net.URLDecoder).

## Usage

- Use the wrap-params function located in the alt-params-middleware.core namespace in
  the same manner you would use the wrap-params in ring.middleware.params
- A custom decoder function can be provided as the :decoder option. 
- The decoder function accepts a url-encoded-string as the first argument, and the encoding as the second argument and returns the decoded string.

``` clj
(wrap-params handler {:encoding "Shift_JIS"
                      :decoder commons-decoder})

```

The commons-decoder mentioned above is implemented here as a reference: 
https://github.com/iku000888/alt-params-middleware/blob/master/src/alt_params_middleware/core.clj#L10

## Acknowledgement
Obviously most of the code is copy/pasted from ring.middleware.params and ring.util.codec.

## License

Copyright © 2016 Ikuru K

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
