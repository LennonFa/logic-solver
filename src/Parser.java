import java.util.ArrayList;
public class Parser {

    private final ArrayList<String> tokens;
    private final boolean[] values;
    private int position = 0;

    Parser(ArrayList<String> tokens, boolean[] values) {
        this.tokens = tokens;
        this.values = values;
    }

    boolean parse(){
        boolean value = parseOr();
        if (position != tokens.size()) {
            throw new IllegalArgumentException("FORMULA ERROR: ");
        }

        return value;
    }


    boolean parseOr() {
        boolean value = parseAnd();

            while (position < tokens.size() && tokens.get(position).equals("||") ) {
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
             throw new IllegalArgumentException("Variable is expected");
        }
        if (tokens.get(position).equals("!")) {
            position++;
            boolean value = parseUnary();
            return !value;
        }

        return parsePrimary();
    }

    boolean parsePrimary() {
        if (tokens.get(position).equals("(")){
            position++;
            boolean value = parseOr();

            if (position >= tokens.size()) {
                throw new IllegalArgumentException("closing parenthesis is missing!");

            } else if (tokens.get(position).equals(")")) {
                position++;
                return value;

            } else {
                throw new IllegalArgumentException("closing parenthesis was expected");
            }
        }

        String token = tokens.get(position);            //token = tokens at current parse position

        if (!Character.isLetter(token.charAt(0))){
            throw new IllegalArgumentException("no valid variable");
        }
        int index = token.charAt(0) - 'a';              //index = firsCharacter of the token - 'a'     (c - a = 2)
        boolean value = values[index];                  //boolean value in values at index (values[2])
        position++;
        return value;                                   //return the value (true/false)
    }
}
