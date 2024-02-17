/**
 *
 * Description
 *
 * @version 1.0 from 17.02.2024
 * @author Nico_44
 */

import java.io.*;
import java.util.*;
import java.lang.Math;
public class Calculator {
    public static Scanner input = new Scanner(System.in);
    public static final String userDir = System.getProperty("user.home");
    public static final String projectFolder = "nico-calculator";
    public static boolean showDebug = false;
    public static HashMap<String, Double> variables = new HashMap<>();
    public static String[] formulas = new String[]{
            "Area of a rectangle: A = length * width",
            "Area of a triangle: A = 1/2 * base * height",
            "Area of a circle: A = PI*r^2",
            "Perimeter of a rectangle: P = 2*(length+width)",
            "Perimeter of a circle (circumference): C = 2*PI*r"
    };

    public static String[] commands = new String[]{
            "'vars' get a list of all variables.",
            "'formulas' get a list of basic math formulas",
            "'set' to set or edit a variable. 'set x = 5'",
            "'remove' to remove a variable. 'remove x'",
            "Type any mathematical equation and it will calculate it."
    };



    public static void main(String[] args) {
        createFolder(userDir, projectFolder);
        loadVariables(userDir +"/"+ projectFolder);

        System.out.println("Nico's simple calculator\n" + "Type 'help' to get a list of commands.");

        variables.put("PI", Math.PI);
        variables.put("E", Math.E);

        while(true) {
            try{
                start();
            } catch(Exception e){
                System.out.println("This input is not valid, type 'help' for a list of commands.\n");
                if(showDebug) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void start() {
        System.out.print("> ");
        String nextLineInput = input.nextLine();

        if(handleCommands(nextLineInput)) return;

        String[] tokens = tokenize(nextLineInput);
        while(Arrays.toString(tokens).contains("+") || Arrays.toString(tokens).contains("*") || Arrays.toString(tokens).contains("/") || Arrays.toString(tokens).contains("^")) {
            tokens = calc(tokens, 0, tokens.length);
            tokens = removeDoneCalcs(tokens);
        }

        System.out.println("= " + Double.parseDouble(tokens[0]) + "\n");
    }

    public static boolean handleCommands(String nextLineInput) {
        if(nextLineInput.equals("vars")) {
            System.out.println("\nVariables: ");
            for(String key : variables.keySet()) {
                System.out.println(" - " + key + " = " + variables.get(key));
            }
            System.out.println("");
            return true;
        }

        if(nextLineInput.startsWith("remove")) {
            String str = nextLineInput.toUpperCase();
            String[] parts = str.split(" ");

            if(!variables.containsKey(parts[1])) {
                System.out.println("Variable '" + parts[1] + "' doesn't exist.\n");
                return true;
            }

            variables.remove(parts[1]);
            System.out.println("Variable '" + parts[1] + "' has been removed.\n");
            saveVariables(userDir + "/nico-calculator");
            return true;
        }

        if(nextLineInput.startsWith("set")) {
            setVariables(nextLineInput);
            return true;
        }

        if(nextLineInput.equals("formulas")) {
            System.out.println("\nFormulas: ");
            for(String formula : formulas) {
                System.out.println(" - " + formula);
            }
            System.out.println("");
            return true;
        }

        if(nextLineInput.equals("help") || nextLineInput.equals("?")) {
            System.out.println("\nCommands: ");
            for(String command : commands) {
                System.out.println(" - " + command);
            }
            System.out.println("");
            return true;
        }

        if(nextLineInput.equals("debug")) {
            showDebug = !showDebug;
            System.out.println("Toggled debug mode to " + (showDebug?"on":"off"));
            return true;
        }

        if(nextLineInput.startsWith("function")) {
            handleFunctions(nextLineInput);
            return true;
        }

        return false;
    }

    public static String[] calc(String[] tokens, int start, int end) {
        String[] brackets = new String[]{"(",")","{","}"};
        boolean hasCalculated = false;
        double result = 0;
        int index = 0;

        for(int x = 0;x<brackets.length;x+=2) {
            String startBracket = brackets[x];
            String endBracket = brackets[x+1];
            if(Arrays.toString(tokens).contains(startBracket) && !hasCalculated) {
                for(int i = start;i<end;i++) {
                    if(tokens[i].equals(startBracket)) {
                        for(int j = i;j<end;j++) {
                            if(tokens[j].equals(endBracket)) {
                                tokens[i] = "REMOVE";
                                tokens[j] = "REMOVE";
                                tokens = removeDoneCalcs(tokens);
                                return calc(tokens, i+1, j-1);
                            }
                        }
                    }
                }
            }
        }

        if(Arrays.toString(tokens).contains("^") && !hasCalculated) {
            for(int i = start;i<end;i++) {
                if(tokens[i].equals("^")) {
                    result = Math.pow(Double.parseDouble(tokens[i-1]), Double.parseDouble(tokens[i+1]));
                    hasCalculated = true;
                    index = i;
                    break;
                }
            }
        }

        if(Arrays.toString(tokens).contains("*") && !hasCalculated) {
            for(int i = start;i<end;i++) {
                if(tokens[i].contains("*")) {
                    result = Double.parseDouble(tokens[i-1]) * Double.parseDouble(tokens[i+1]);
                    hasCalculated = true;
                    index = i;
                    break;
                }
            }
        }

        if(Arrays.toString(tokens).contains("/") && !hasCalculated) {
            for(int i = start;i<end;i++) {
                if(tokens[i].contains("/")) {
                    result = Double.parseDouble(tokens[i-1]) / Double.parseDouble(tokens[i+1]);
                    hasCalculated = true;
                    index = i;
                    break;
                }
            }
        }

        if(Arrays.toString(tokens).contains("+") && !hasCalculated) {
            for(int i = start;i<end;i++) {
                if(tokens[i].contains("+")) {
                    result = Double.parseDouble(tokens[i-1]) + Double.parseDouble(tokens[i+1]);
                    hasCalculated = true;
                    index = i;
                    break;
                }
            }
        }

        if(hasCalculated) {
            tokens[index] = String.valueOf(result);
            tokens[index-1] = "REMOVE";
            tokens[index+1] = "REMOVE";
            tokens = removeDoneCalcs(tokens);
        }
        return tokens;
    }
    public static String[] removeDoneCalcs(String[] tokens) {
        List<String> cleanedTokens = new ArrayList<>();
        for(int i = 0;i<tokens.length;i++) {
            if(!tokens[i].equals("REMOVE")) {
                cleanedTokens.add(tokens[i]);
            }
        }

        return cleanedTokens.toArray(new String[cleanedTokens.size()]);
    }
    public static String[] tokenize(String str) {
        if(str.length() <= 0) return new String[]{"0"};
        str = str.toUpperCase();
        str = str.replace(" ", "");

        String[] splittedString = str.split("(?=[)(*+/^-])|(?<=[)(*+/^-])");
        List<String> tokens = new ArrayList<>();

        for(String token : splittedString) {
            for(String key : variables.keySet()) {
                if(key.equals(token)) {
                    token = String.valueOf(variables.get(key));
                }
            }
            tokens.add(token);

            String[] newTokens = negativeConvertion(tokens.toArray(new String[tokens.size()]));
            tokens.removeAll(tokens);
            for(String innerToken : newTokens) {
                tokens.add(innerToken);
            }
        }

        if(showDebug) {
            System.out.println("TOKENS: " + tokens.toString());
        }
        return tokens.toArray(new String[tokens.size()]);
    }
    public static String[] negativeConvertion(String[] tokens) {
        for(int i = 0;i<tokens.length-1;i++) {
            if(tokens[i].equals("-")) {
                if(i == 0 || tokens[i-1].equals("(")) {
                    tokens[i] = "REMOVE";
                } else {
                    tokens[i] = "+";
                }
                tokens[i+1] = "-"+tokens[i+1];
            }
        }
        tokens = removeDoneCalcs(tokens);
        return tokens;
    }

    public static void setVariables(String str) {
        str = str.replace(" ", "");
        str = str.replace("set", "");
        str = str.toUpperCase();

        String[] strParts = str.split("(?=[=])|(?<=[=])");
        String[] varTokens = tokenize(strParts[2]);

        while(Arrays.toString(varTokens).contains("+") || Arrays.toString(varTokens).contains("*") || Arrays.toString(varTokens).contains("/") || Arrays.toString(varTokens).contains("^")) {
            varTokens = calc(varTokens, 0, varTokens.length);
            varTokens = removeDoneCalcs(varTokens);
        }

        if(!variables.containsKey(strParts[0])) {
            variables.put(strParts[0], Double.parseDouble(varTokens[0]));
            System.out.println("\nVariable '" + strParts[0] + "' has been set with the value: " + varTokens[0] + "\n");
        } else {
            double pastValue = variables.get(strParts[0]);

            variables.replace(strParts[0], Double.parseDouble(varTokens[0]));
            System.out.println("\nVariable '" + strParts[0] + "' has been set with the value: " + varTokens[0] + "\nPrevious value was: " + pastValue + "\n");
        }
        saveVariables(userDir + "/nico-calculator");
    }

    //Save and Load, variables

    public static void createFolder(String path, String folderName) {
        File folder = new File(path, folderName);

        if(!folder.exists()) {
            folder.mkdir();
            saveVariables(path +"/"+ folderName);
        }
    }

    public static void saveVariables(String path) {
        File file = new File(path, "/variables.txt");

        try{
            FileWriter writer = new FileWriter(file);

            String json = "";

            for(String var : variables.keySet()) {
                json += var + ": " + variables.get(var) + "\n";
            }

            writer.write(json);
            writer.close();
        } catch(IOException e) {
            System.out.println("There has been an error saving your configuration: " + e.getMessage());
        }
    }

    public static void loadVariables(String path) {
        File file = new File(path, "/variables.txt");

        try{
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String line;
            while((line = bufferedReader.readLine()) != null) {
                String[] parts = new String[2];
                line = line.replace(" ", "");
                parts = line.split(":");
                parts[1] = parts[1].replace(";", "");
                variables.putIfAbsent(parts[0], Double.parseDouble(parts[1]));
            }

            bufferedReader.close();
            fileReader.close();

        } catch(IOException e) {
            System.out.println("There has been an error loading your configuration: " + e.getMessage());
        }
    }

    public static void handleFunctions(String str) {
        str = str.toUpperCase();
        str = str.replace("FUNCTION", "");

        String resultSpacer = "", inputSpacer = "";

        double startValue = 0d, endValue = 10d, stepValue = 0d;
        int inputLength = 0, resultLength = 0;

        System.out.print("\nStart value (0): ");
        startValue = input.nextDouble();
        System.out.print("\nEnd value (10): ");
        endValue = input.nextDouble();
        System.out.print("\nStep value (1): ");
        stepValue = input.nextDouble();

        HashMap<String, String> results = new HashMap<String,String>();

        for(double i = startValue;i<endValue;i+=stepValue) {
            String[] tokens = tokenize(str);
            for(int j = 0;j<tokens.length;j++) {
                if(tokens[j].equals("X")) {
                    tokens[j] = String.valueOf(i);
                }
            }

            while(Arrays.toString(tokens).contains("+") || Arrays.toString(tokens).contains("*") || Arrays.toString(tokens).contains("/") || Arrays.toString(tokens).contains("^")) {
                tokens = calc(tokens, 0, tokens.length);
                tokens = removeDoneCalcs(tokens);
            }
            if(resultLength < tokens[0].length()) {
                resultLength = tokens[0].length()+2;
            }
            if(inputLength < String.valueOf(i).length()) {
                inputLength = String.valueOf(i).length()+2;
            }
            results.put(String.valueOf(i),tokens[0]);
        }

        for(int i = 0;i<inputLength;i++) {

            inputSpacer += "-";
        }
        for(int i = 0;i<resultLength;i++) {
            resultSpacer += "-";
        }

        for(String key : results.keySet()) {
            System.out.println("X: " + key + " Y: " + results.get(key));
        }
    }

}
