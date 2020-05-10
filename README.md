# jlox
This is a tree-walk interpreter implemented in Java. Its design draws inspiration from the C-style family of languages
(syntactically) and borrows elements from functional style languages such as Lisp and Haskell for optimizations. Some
notable features include:

<h2> Lexical scoping </h2>
jlox will scope appropriately and allow for nesting and closures without additional keywords, unlike other languages
like Python, Ruby, and JavaScript.

<h2> First-class functions </h2>
jlox draws inspiration from the functional paradigm of programming languages by allowing functions to be passed as values
to other functions, allowing for flexibility and the ability to curry (chain together) multiple function calls.
