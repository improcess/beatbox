# beatbox

Basic sample lopper triggered by button presses

## Usage
First up, stick some samples in the assets dir. Wav or aiff files will do.

Next, fire up a REPL with lein:

    lein repl
    > (use 'beatbox.core)

Jam on keys!

## Installation

Clone into a dir on your machine:

    git clone git://github.com/improcess/beatbox.git

Run `merge-checkout-deps.rb`

    ./merge-checkout-deps.rb

Edit src/beatbox/core to point to your monome:
    (def m (poly/init "/dev/tty.usbserial-m64-0790"))

That's it!

## License

Copyright (C) 2010 FIXME

Distributed under the Eclipse Public License, the same as Clojure.
