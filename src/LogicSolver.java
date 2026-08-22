import java.util.ArrayList;
import java.util.Scanner;

public class LogicSolver {
    public static void main(String[] args){
        //boolean[] values = {false, true};
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter formula: ");
        String formula = scanner.nextLine();

        System.out.println("How many variables? ");
        int variableCount = scanner.nextInt();

        int combinations = 1 << variableCount;
        int solution = -1;
        int checks = 0;
        boolean[] variables = new boolean[variableCount];
        boolean satisfiable = false;

        ArrayList<String> tokens = new ArrayList<>();

        for (int i = 0; i < formula.length(); i++){
            char c = formula.charAt(i);
            if (c == ' '){
                continue;
            }
            if (c == '&' && i + 1 < formula.length()){
                if (formula.charAt(i+1) == '&'){
                    tokens.add("&&");
                    System.out.println("AND detected");
                    i++;
                    continue;
                }
            }
            if (c == '|' && i + 1 < formula.length()){
                if (formula.charAt(i+1) == '|'){
                    tokens.add("||");
                    System.out.println("OR detected");
                    i++;
                    continue;
                }
            }

            if (c == '(' || c == ')' || c == '!'){

                tokens.add(String.valueOf(c));
            }


            if (Character.isLetter(c)){
                tokens.add(String.valueOf(c));
            }
            System.out.println(c);
        }
        for (String token : tokens){
            System.out.println(token);
        }

        System.out.println("You entered: " + formula);

        for (int i = 0; i < combinations; i ++){
            checks++;

            for (int bit = 0; bit < variableCount; bit++){
                int mask = 1 << bit;

                variables[bit] = (i & mask) != 0;
            }
            //boolean result = evaluateOld(tokens, variables);

            Parser parser = new Parser(tokens, variables);
            boolean result = parser.parse();

            if (result){
                satisfiable = true;
                solution = i;
                break;}
        }
        if (satisfiable){
            System.out.println("SAT");
            System.out.println("Checks to complete: " + checks);
            for (int bit = 0; bit < variableCount; bit++){
                int mask = 1 << bit;

                boolean value = (solution & mask) != 0;

                System.out.println("Variable["+ bit + "]" + value);

            }
        } else {
            System.out.println("UNSAT");
            System.out.println("Checks to complete: " + checks);
        }
    }

    static boolean evaluateOld(ArrayList<String> tokens, boolean[] values) {

        boolean currentGroup;
        int indexOpenBracket = -1;
        int indexCloseBracket = -1;
        ArrayList<Integer> bracketsStack = new ArrayList<>();
        ArrayList<Integer> bracketPeers = new ArrayList<>();

        //search bracket
        for (int j = 0; j < tokens.size(); j++){
            if (tokens.get(j).equals("(")){
                bracketsStack.add(j);
            }
            if (tokens.get(j).equals(")")){
                bracketPeers.add(bracketsStack.getLast());
                bracketPeers.add(j);
                bracketsStack.removeLast();
            }
        }
        int f = 0;
        while (f < bracketPeers.size() - 1){
            System.out.println("Peer " + bracketPeers.get(f) + " ... " + bracketPeers.get(f + 1));
            f += 2;
        }


        //build innerTokens
        ArrayList<String> innerTokens = new ArrayList<>();

        for (int l = 0; l < bracketPeers.size(); l += 2){
            indexOpenBracket = bracketPeers.get(l);
            indexCloseBracket = bracketPeers.get(l+1);

            int tokenIndex = indexOpenBracket + 1;
            while (tokenIndex < indexCloseBracket){
                innerTokens.add(tokens.get(tokenIndex));
                tokenIndex++;
                System.out.println("InnerTokens now : " + innerTokens);
            }
            l++;
        }

        //set first value
        String firstToken = tokens.get(0);
        int i;
        boolean resultSoFar = false;

        if (tokens.get(0).equals("(")){
            boolean bracketResult = evaluateOld(innerTokens, values);
            currentGroup = bracketResult;
            i = indexCloseBracket + 1;
        } else if (tokens.get(0).equals("!")) {
            i = 2;
            currentGroup = !values[tokens.get(1).charAt(0) - 'a'];

        } else {
            i = 1;
            int firstIndex = firstToken.charAt(0) - 'a';
            currentGroup = values[firstIndex];
        }

        //calculate
        while (i < tokens.size()){
            if (tokens.get(i).equals("&&")){
                if (tokens.get(i + 1).equals("!")){
                    int indexRight = tokens.get(i + 2).charAt(0) - 'a';
                    boolean valueRight = !values[indexRight];
                    currentGroup = currentGroup && valueRight;
                    i++;
                } else if (tokens.get(i  + 1).equals("(")){
                    boolean valueRight = evaluateOld(innerTokens, values);
                    currentGroup = currentGroup && valueRight;
                    i = indexCloseBracket + 1;
                    continue;
                } else {
                    int indexRight = tokens.get(i + 1).charAt(0) - 'a';
                    boolean valueRight = values[indexRight];
                    currentGroup = currentGroup && valueRight;
                }

            } else if (tokens.get(i).equals("||")) {
                if (tokens.get(i + 1).equals("!")){
                    int indexRight = tokens.get(i + 2).charAt(0) - 'a';
                    boolean valueRight = !values[indexRight];
                    resultSoFar = resultSoFar || currentGroup;
                    currentGroup = valueRight;
                    i++;
                } else if (tokens.get(i + 1).equals("(")){
                    boolean valueRight = evaluateOld(innerTokens, values);
                    resultSoFar = resultSoFar || currentGroup;
                    currentGroup = valueRight;
                    i = indexCloseBracket + 1;
                    continue;
                } else {
                    int index = tokens.get(i + 1).charAt(0) - 'a';
                    boolean valueRight = values[index];
                    resultSoFar = resultSoFar || currentGroup;
                    currentGroup = valueRight;
                }

            } else {
                throw new IllegalArgumentException("error");
            }
            i+= 2;
        }
        resultSoFar = resultSoFar || currentGroup;
        return resultSoFar;
    }
}
