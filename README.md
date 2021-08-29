# drepl

The Node JS repl with easy library installations

## Installation

Download from https://github.com/claytn/drepl

## Usage

As of now, I haven't been able to publish a binary executable. So, jar files it is!

$ clj -X:uberjar

```
Welcome to drepl!
The node repl with quick library installations

Usage: java -jar drepl.jar [options] [npm-package-names...]

Dependencies:
  npm & npx must be available in your $PATH

Options:
  -h, --help
  -t, --typescript  Use ts-node engine (not fully supported)
  -c, --clean       Clean up all previously used dependencies
```

## Examples

    $ java -jar drepl.jar ramda

Copyright © 2021

_EPLv1.0 is just the default for projects generated by `clj-new`: you are not_
_required to open source this project, nor are you required to use EPLv1.0!_
_Feel free to remove or change the `LICENSE` file and remove or update this_
_section of the `README.md` file!_

Distributed under the Eclipse Public License version 1.0.