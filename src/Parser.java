import java.util.ArrayList;
public class Parser {

    private final ArrayList<String> tokens;
    private final boolean[] values;
    private int position = 0;

    Parser(ArrayList<String> tokens, boolean[] values){
        this.tokens = tokens;
        this.values = values;
    }

    boolean parsePrimary(){
        String token = tokens.get(position);            //token = tokens at current parse position
        int index = token.charAt(0) - 'a';              //index = firsCharacter of the token - 'a'     (c - a = 2)
        boolean value = values[index];                  //boolean value in values at index (values[2])
        position++;
        return value;                                   //return the value (true/false)
    }
}
