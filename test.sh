#!/bin/sh
set -eu
cd "$(dirname "$0")"
mkdir -p build/classes
javac --release 17 -d build/classes src/LogicSolver.java src/Parser.java tests/LogicSolverTest.java
java -cp build/classes LogicSolverTest
