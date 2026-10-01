import java.util.ArrayList;
import java.util.Scanner;

public class LogicSolver {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Variables valid: a - z");
            System.out.println("Enter formula:");

            if (!scanner.hasNextLine()) {
                System.err.println("Error: No input.");
                System.exit(1);
                return;
            }

            String formula = scanner.nextLine();
            try {
                Result result = solve(formula);
                System.out.println("You entered: " + formula);
                System.out.println(result.satisfiable ? "SAT" : "UNSAT");
                System.out.println("Checks to complete: " + result.checks);

                if (result.satisfiable) {
                    for (char name : result.names) {
                        System.out.println(name + " = " + result.values[name - 'a']);
                    }
                }
            } catch (IllegalArgumentException e) {
                System.err.println("Error: " + e.getMessage());
                System.exit(1);
            }
        }
    }

    static Result solve(String formula) {
        ArrayList<String> tokens = tokenize(formula);
        boolean[] present = new boolean[26];

        for (String token : tokens) {
            char c = token.charAt(0);
            if (c >= 'a' && c <= 'z') {
                present[c - 'a'] = true;
            }
        }

        ArrayList<Character> names = new ArrayList<>();
        for (int i = 0; i < present.length; i++) {
            if (present[i]) {
                names.add((char) ('a' + i));
            }
        }

        int combinations = 1 << names.size();
        boolean[] values = new boolean[26];
        int checks = 0;

        for (int i = 0; i < combinations; i++) {
            // Only used variables get a bit; the parser still indexes values by a-z.
            for (int bit = 0; bit < names.size(); bit++) {
                values[names.get(bit) - 'a'] = (i & (1 << bit)) != 0;
            }

            checks++;
            Parser parser = new Parser(tokens, values);
            if (parser.parse()) {
                return new Result(true, checks, names, values);
            }
        }

        return new Result(false, checks, names, values);
    }

    static ArrayList<String> tokenize(String formula) {
        if (formula == null || formula.isBlank()) {
            throw new IllegalArgumentException("No input.");
        }

        ArrayList<String> tokens = new ArrayList<>();
        for (int i = 0; i < formula.length(); i++) {
            char c = formula.charAt(i);
            if (Character.isWhitespace(c)) {
                continue;
            }

            if (c == '&' || c == '|') {
                if (i + 1 < formula.length() && formula.charAt(i + 1) == c) {
                    tokens.add(c == '&' ? "&&" : "||");
                    i++;
                    continue;
                }
                throw new IllegalArgumentException(
                        "Expected '" + c + c + "' at position " + (i + 1) + ".");
            }

            if (c == '(' || c == ')' || c == '!' || (c >= 'a' && c <= 'z')) {
                tokens.add(String.valueOf(c));
                continue;
            }

            throw new IllegalArgumentException(
                    "Invalid character '" + c + "' at position " + (i + 1) + ".");
        }
        return tokens;
    }

    static class Result {
        final boolean satisfiable;
        final int checks;
        final ArrayList<Character> names;
        final boolean[] values;

        Result(boolean satisfiable, int checks, ArrayList<Character> names, boolean[] values) {
            this.satisfiable = satisfiable;
            this.checks = checks;
            this.names = new ArrayList<>(names);
            this.values = values.clone();
        }
    }
}
