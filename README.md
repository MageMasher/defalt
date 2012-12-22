# defalt

##"Let me comment this out for a second..."

## Rationale

REPL-driven development is very powerful, but it comes with unique
challenges which are not found in other approraches. The usual
edit-compile-run cycle becomes a bit more chaotic because of the
potential of selectively evaluating parts of the code. In many cases
"the code" is not even in your code files, it's just a transient line
that you typed into your REPL which introduced a new symbol in your
namespace.

When debugging existing code, the REPL offers the unique opportunity
to replace functions with alternative versions of themselves
interactively and at runtime. In an attempt to identify the bug,
portions of the code are commented out, return values are mocked and
namespaces are maimed and tortured.

How do you manage all that? There are various approaches: you may copy
and paste the relevant function into the REPL, change it a bit and
press enter, thus making an invisible change that you will forget
about in half an hour. You can change your code in place, with a
";;TODO remove this" and happily ignore your comment and deploy to
your mocked code to production at the end of a long a difficult day.

defalt attempts to provide a way out. It provides the tools to define
alternative implementation of methods, to dynamically switch between
those alternatives, and to easily inspect which version is currently
in effect.

This project is very new, and still under discussion, so expect the
syntax and API to change. I'm also very keen for feedback to make sure
that this will go in the right direction.

## Usage

Here is some code using defalt:

```clojure
   (require '[defalt :as da])
   (use '[defalt :only [defalt]])
    
   (defn add-numbers [a b]
     (+ a b))
    
   (defalt add-numbers|memo fun [a b]
     (memoize fun))
    
   (defalt add-numbers|mock [a b] 5)
```

And here is what you can do in the REPL based on the code above:

```clojure
   > (da/show add-numbers)
    
   Alternatives for add-numbers:
   * 0. <<master>>
     1. memo
     2. mock
    
   > (da/switch add-numbers memo)
   > (da/show add-numbers)
    
   Alternatives for add-numbers:
     0. <<master>>
   * 1. memo
     2. mock
    
   > (da/switch 2) ;;defaults to last function
   > (da/show)
    
   Alternatives for add-numbers:
     0. <<master>>
     1. memo
   * 2. mock
    
   > (da/reset)
   > (da/show)
    
   Alternatives for add-numbers:
   * 0. <<master>>
     1. memo
     2. mock
    
   > (da/code add-numbers mock)
    
   (defalt add-numbers|mock [a b] 5)
```

## License

Copyright © 2012 Stathis Sideris

Distributed under the Eclipse Public License, the same as Clojure.
