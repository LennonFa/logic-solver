import java.util.ArrayList;

public class Parser {
    private final ArrayList<String> tokens;
    private final boolean[] values;
    private int position = 0;

    Parser(ArrayList<String> tokens, boolean[] values) {
        this.tokens = tokens;
        this.values = values;
    }

    boolean parse() {
        boolean value = parseOr();
        if (position != tokens.size()) {
            throw new IllegalArgumentException("Unexpected token '" + tokens.get(position) + "'.");
        }
        return value;
    }

    boolean parseOr() {
        boolean value = parseAnd();

        while (position < tokens.size() && tokens.get(position).equals("||")) {
            position++;
            boolean right = parseAnd();
            value = value || right;
        }
        return value;
    }

    boolean parseAnd() {
        boolean value = parseUnary();

        while (position < tokens.size() && tokens.get(position).equals("&&")) {
            position++;
            boolean right = parseUnary();
            value = value && right;
        }
        return value;
    }

    boolean parseUnary() {
        if (position >= tokens.size()) {
            throw new IllegalArgumentException("Expected a variable or '('.");
        }
        if (tokens.get(position).equals("!")) {
            position++;
            return !parseUnary();
        }
        return parsePrimary();
    }

    boolean parsePrimary() {
        if (tokens.get(position).equals("(")) {
            position++;
            boolean value = parseOr();

            if (position >= tokens.size() || !tokens.get(position).equals(")")) {
                throw new IllegalArgumentException("Expected ')'.");
            }
            position++;
            return value;
        }

        String token = tokens.get(position);
        if (token.length() != 1 || token.charAt(0) < 'a' || token.charAt(0) > 'z') {
            throw new IllegalArgumentException("Expected a variable or '(', found '" + token + "'.");
        }

        int index = token.charAt(0) - 'a';
        boolean value = values[index];
        position++;
        return value;
    }
}
