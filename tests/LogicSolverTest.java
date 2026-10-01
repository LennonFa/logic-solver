import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class LogicSolverTest {
    private static int checks = 0;

    public static void main(String[] args) throws Exception {
        testSolver();
        testParser();
        testInvalidInput();
        testCli();
        System.out.println("Passed " + checks + " checks.");
    }

    private static void testSolver() {
        checkResult("a", true, 2);
        checkResult("a && !a", false, 2);
        checkResult("a || !a", true, 1);
        checkResult("(a || b) && !a && !b", false, 4);
        checkResult("!(a && b) && a && b", false, 4);
        checkResult("!!a", true, 2);
        checkResult("z && !z", false, 2);
        checkResult("z || !z", true, 1);
        checkResult("z && z", true, 2);
        checkResult(" \t!a\n&& z\r ", true, 3);

        LogicSolver.Result result = LogicSolver.solve("!a && z");
        equal(Arrays.asList('a', 'z'), result.names, "only a and z");
        equal(false, result.values[0], "a in the solution");
        equal(true, result.values[25], "z in the solution");

        String allVariables = "a || b || c || d || e || f || g || h || i || j || k || l || m"
                + " || n || o || p || q || r || s || t || u || v || w || x || y || z";
        result = LogicSolver.solve(allVariables);
        equal(26, result.names.size(), "all variable names");
        equal(2, result.checks, "all variables fit in the enumeration");
    }

    private static void testParser() {
        for (int mask = 0; mask < 8; mask++) {
            boolean a = (mask & 1) != 0;
            boolean b = (mask & 2) != 0;
            boolean c = (mask & 4) != 0;
            boolean[] values = new boolean[26];
            values[0] = a;
            values[1] = b;
            values[2] = c;

            equal(a || (b && c), evaluate("a || b && c", values), "AND before OR");
            equal((a || b) && c, evaluate("(a || b) && c", values), "parentheses");
            equal((!a) && b, evaluate("!a && b", values), "NOT before AND");
            equal(a, evaluate("!!a", values), "double negation");
            equal((!a) || (!b), evaluate("!(a && b)", values), "De Morgan");
            equal(a && (b || (!c)), evaluate("a && (b || !c)", values), "consume the right side");
        }
    }

    private static void testInvalidInput() {
        String[] invalid = {
                "", " \t\n", "a &&", "a ||", "!", "()", "(a", "a)", "(a || b",
                "a b", "a(b)", "&& a", "a & b", "a | b", "a &&& b", "a ||| b",
                "a || (b &&)", "a || (b &&", "A", "1", "a ^ b"
        };
        for (String formula : invalid) {
            try {
                LogicSolver.solve(formula);
                throw new AssertionError("Accepted invalid formula: " + formula);
            } catch (IllegalArgumentException expected) {
                checks++;
            }
        }

        // A true left operand must not hide invalid syntax on the right.
        boolean[] values = new boolean[26];
        values[0] = true;
        try {
            evaluate("a || (b &&)", values);
            throw new AssertionError("Short-circuiting skipped invalid syntax.");
        } catch (IllegalArgumentException expected) {
            checks++;
        }
    }

    private static void testCli() throws Exception {
        checkCli("!a && z\n", 0, "a = false\nz = true", "");
        checkCli("z && !z\n", 0, "UNSAT\nChecks to complete: 2", "");
        checkCli("a &&\n", 1, "", "Error: Expected a variable or '('.");
        checkCli("\n", 1, "", "Error: No input.");
        checkCli("", 1, "", "Error: No input.");
    }

    private static void checkResult(String formula, boolean satisfiable, int expectedChecks) {
        LogicSolver.Result result = LogicSolver.solve(formula);
        equal(satisfiable, result.satisfiable, formula + " satisfiability");
        equal(expectedChecks, result.checks, formula + " assignment count");
        if (satisfiable) {
            equal(true, evaluate(formula, result.values), formula + " returned assignment");
        }
    }

    private static boolean evaluate(String formula, boolean[] values) {
        return new Parser(LogicSolver.tokenize(formula), values).parse();
    }

    private static void checkCli(String input, int exitCode, String output, String error)
            throws Exception {
        String executable = System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java";
        String java = Path.of(System.getProperty("java.home"), "bin", executable).toString();
        Process process = new ProcessBuilder(java, "-cp", System.getProperty("java.class.path"),
                "LogicSolver").start();
        try (var stdin = process.getOutputStream()) {
            stdin.write(input.getBytes(StandardCharsets.UTF_8));
        }
        if (!process.waitFor(5, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            throw new AssertionError("CLI did not finish.");
        }
        String stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8)
                .replace("\r\n", "\n");
        String stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8)
                .replace("\r\n", "\n");
        equal(exitCode, process.exitValue(), "CLI exit code");
        equal(true, stdout.contains(output), "CLI output: " + output);
        equal(true, error.isEmpty() ? stderr.isEmpty() : stderr.contains(error), "CLI error");
        equal(false, stderr.contains("Exception") || stderr.contains("\tat "), "no stacktrace");
        if (exitCode == 0) {
            equal(false, stdout.contains("Variable["), "actual variable names");
        } else {
            equal(false, stdout.contains("\nSAT\n") || stdout.contains("\nUNSAT\n"),
                    "invalid input has no result");
        }
    }

    private static void equal(Object expected, Object actual, String label) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(label + ": expected " + expected + ", got " + actual);
        }
        checks++;
    }
}
