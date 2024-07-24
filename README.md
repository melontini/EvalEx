EvalEx - Java Expression Evaluator
==========

This is a personal fork of EvalEx with lots of incompatible and unstable changes. 

Please check out the original repo here on [GitHub](https://github.com/ezylang/EvalEx).

Changes over upstream:

- Inlining of simple and constant expressions.
- Lazy array and list conversions.
- String multiplication and number conversion.
```
"Hello" * 3 => "HelloHelloHello"
```
- `DataAccessorIfc` data type.
- Immutable contexts and expressions.
- Replaced expression-wide variables with `evaluate` only parameters.
- `Object[]` in various places to allow passing arbitrary objects to data accessors and custom functions.
- Constant are no longer copied to each expression.
- Data accessors can now throw EvaluationExceptions.
- Custom identifier characters.
- Removed dictionary interfaces.
- Switched from maven to gradle.
- And more...

## Author and License

Copyright 2012-2023 by Udo Klimaschewski

**Thanks to all who contributed to this
project: [Contributors](https://github.com/ezylang/EvalEx/graphs/contributors)**

The software is licensed under the Apache License, Version 2.0 (
see [LICENSE](https://raw.githubusercontent.com/ezylang/EvalEx/main/LICENSE) file).

* The *power of* operator (^) implementation was copied
  from [Stack Overflow](http://stackoverflow.com/questions/3579779/how-to-do-a-fractional-power-on-bigdecimal-in-java)
  Thanks to Gene Marin
* The SQRT() function implementation was taken from the
  book [The Java Programmers Guide To numerical Computing](http://www.amazon.de/Java-Number-Cruncher-Programmers-Numerical/dp/0130460419) (
  Ronald Mak, 2002)
