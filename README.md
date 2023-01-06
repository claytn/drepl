# drepl

The Node JS repl with easy library installations

## Installation

Step 1: install babashka

> drepl is a [babashka](https://babashka.org/) script so you'll need babashka on your local machine to run drepl.

```sh
brew install borkdude/brew/babashka
```

Step 2: install drepl

```sh
wget -O /usr/local/bin/drepl https://raw.githubusercontent.com/claytn/drepl/master/drepl.clj \
&& chmod +x /usr/local/bin/drepl
```

## Usage

```
Welcome to drepl!
The node repl with quick library installations

Usage: drepl [option]... [npm-package]...

Dependencies:
  npm must be available in your $PATH

Options:
  -h, --help
  -c, --clean       Clean up all previously used dependencies
```

## Example

```sh
drepl lodash
```

The above command will load a node repl with access to the npm dependency, [lodash](https://lodash.com/).
```
Welcome to Node.js v16.16.0.
Type ".help" for more information.
> const _ = require('lodash')
undefined
> _.flatten([[1, 2], [3, 4]])
[ 1, 2, 3, 4 ]
```

Copyright © 2022
Distributed under the Eclipse Public License version 1.0.
