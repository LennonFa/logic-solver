import java.util.ArrayList;
public class Parser {

    private final ArrayList<String> tokens;
    private final boolean[] values;
    private int position = 0;

    Parser(ArrayList<String> tokens, boolean[] values){
        this.tokens = tokens;
        this.values = values;
    }

    boolean parse() {
        return;
    }
}
