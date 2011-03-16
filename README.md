# beatbox

Basic sample lopper triggered by button presses

## Dependencies

* [Overtone](http://github.com/overtone/overtone)
* [Polynome](http://github.com/improcess/polynome)

## Installation

Clone into a dir on your machine:

    git clone git://github.com/improcess/beatbox.git

Pull in the dependencies:

    cake deps

Edit src/beatbox/core to point to your monome:

    (def m (poly/init "/dev/tty.usbserial-m64-0790"))

Finally, stick some samples in the assets dir. Wav or aiff files will do. Make sure they will all work in sync and th
at they will loop well together.

## Usage

Fire up a REPL with cake:

    > cake repl
    ==> (use 'beatbox.core)

Jam on monome keys!

## Contributors

* Sam Aaron

## License

Copyright (C) 2010, 2011 Sam Aaron

Distributed under the Eclipse Public License, the same as Clojure.
