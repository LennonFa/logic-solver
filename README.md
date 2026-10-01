# Logic Solver

A small Java program I wrote to practise propositional logic and parsing for university. It tries different values for the variables in a formula until it finds a solution.

`SAT` means that at least one assignment makes the formula true. `UNSAT` means that none does.

## Syntax

| Input | Meaning |
| --- | --- |
| `a`–`z` | Boolean variables |
| `!` | NOT |
| `&&` | AND |
| `\|\|` | OR |
| `(...)` | Grouping |

NOT is evaluated before AND, and AND before OR. Parentheses can change this order. Spaces and tabs are ignored. Variable names are single lowercase letters.

## Run

You need JDK 17 or later. There are no external dependencies.

From the project folder:

```sh
mkdir -p build/classes
javac --release 17 -d build/classes src/LogicSolver.java src/Parser.java
java -cp build/classes LogicSolver
```

Enter a formula when prompted. For `!a && z`, the result is:

```text
SAT
Checks to complete: 3
a = false
z = true
```

For `z && !z`:

```text
UNSAT
Checks to complete: 2
```

Only variables used in the formula are included in the search. They are sorted alphabetically, and the first variable changes fastest. Invalid input produces an error message.

## Tests

On macOS or Linux, run:

```sh
sh test.sh
```

You can also compile and run the tests directly after creating `build/classes`:

```sh
javac --release 17 -d build/classes src/LogicSolver.java src/Parser.java tests/LogicSolverTest.java
java -cp build/classes LogicSolverTest
```

`LogicSolverTest` is a separate Java program that compares expected results with actual results. It checks formulas, truth tables, invalid input and the command-line output. If a check fails, it stops with an error.

## How it works

`LogicSolver` splits the input into tokens and collects the variables. The bits of a counter represent their true/false values. `Parser` reads the tokens using separate methods for OR, AND and NOT. Both sides of an operator are parsed before their values are combined.

This is a brute-force search for small formulas. With `n` variables, it may try all `2^n` assignments. The formula is parsed again for each assignment, so the worst-case time is `O(2^n * m)`, where `m` is the number of tokens. More variables quickly make the search slow, and very deeply nested formulas can exceed the recursion limit.
