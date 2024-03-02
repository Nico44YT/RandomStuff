/**
 * @version 1.0 from 22.02.2024
 * @author Nico
 */

import java.util.*;
import java.lang.Math;

public class Calculator2 {
  
  public static boolean debug = false;

  public static final Scanner input = new Scanner(System.in);

  public static HashMap<String, String> operations = new HashMap<>();
  public static HashMap<String, Double> variables = new HashMap<>();
  public static HashMap<String, String> methods = new HashMap<>();
  public static List<String> illegalVariableNames = new ArrayList<>();
  
  public static void main(String[] args) {
    variables.put("PI", Math.PI); //PI
    variables.put("E", Math.E); //Euler
    variables.put("g", 9.80665d); //Earth gravity
    variables.put("c", 299792458d); //Speed of light
    

    while(true) {
      try {
        mainInterface();
      } catch(Exception e) {
        printDebug(e.getMessage());
      }
    }
  }

  public static void mainInterface() {
    boolean handledCommand = false;
    
    operations.put("+", "ADDITION");
    operations.put("*", "MULTIPLICATION");
    operations.put("/", "DIVISION");
    operations.put("-", "SUBTRACTION");
    operations.put("%", "MODULO");
    operations.put("^", "POWER");
    operations.put("sqrt", "SQUARE_ROOT");
    operations.put("log", "LOG_");
    operations.put("root", "ROOT_");
    operations.put("sin", "SINUS");
    operations.put("cos", "COSINUS");
    operations.put("tan", "TANGENS");
    operations.put("arcsin", "ARCUS_SINUS");
    operations.put("arccos", "ARCUS_COSINUS");
    operations.put("arctan", "ARCUS_TANGENS");
    operations.put("(", "BRACKET_OPEN");
    operations.put(")", "BRACKET_CLOSED");
    operations.put("!", "FACTORIAL");
    
    for(String key : operations.keySet()) {
      illegalVariableNames.add(key);
    }
    for(String key : operations.values()) {
      illegalVariableNames.add(key);
    }
  

    System.out.print("\n> ");
    String iString = input.nextLine();
    
    handledCommand = handleCommand(iString);
    
    List<String> tokens = tokenize(iString);
    
    printDebug("\n");
    
    while(tokens.size()>1 && !handledCommand) {
      List<String> prevTokens = tokens;
      tokens = calculate(tokens, 0, tokens.size());
      tokens = removeDoneCalculations(tokens);

      if(tokens.equals(prevTokens)) {
        System.out.println("Syntax error");
        return;
      }
    }
    if(tokens.get(0).equals("NaN")) {

      System.out.println("Math error");

      return;
    }
    if(!handledCommand) System.out.println("  " + tokens.get(0));
  }
  
  public static List<String> tokenize(String str) {
    str = str.replace(" ", "");
    String splitArgs = "(?=[%)!(*+/^-])|(?<=[%)!(*+/^-])";
    List<String> tokens = new ArrayList<String>();
    
    tokens = Arrays.asList(str.split(splitArgs));
    
    for(int i = 0;i<tokens.size();i++) {
      String token = tokens.get(i);
      
      if(operations.containsKey(token)) {
        tokens.set(i, operations.get(token));
      }
     
      if(variables.containsKey(token)) {
        tokens.set(i, String.valueOf(variables.get(token)));
      }

    }

    for(int i = 0;i<tokens.size();i++) {
      String token = tokens.get(i);

      if(token.contains("log")) {
        String newToken = operations.get("log");
        String[] splittedToken = token.split("g");

        newToken += splittedToken[1];
        tokens.set(i, newToken);
      }
    }

    for(int i = 0;i<tokens.size();i++) {
      String token = tokens.get(i);

      if(token.contains("root")) {
        String newToken = operations.get("root");
        String[] splittedToken = token.split("t");

        newToken += splittedToken[1];
        tokens.set(i, newToken);
      }
    }

    for(int i = 1;i<tokens.size();i++) {
      String token = tokens.get(i);
      String prevToken = tokens.get(i-1);

      if("SUBTRACTION".equals(token)) {
        if(operations.containsValue(prevToken)) {
          tokens.set(i, "REMOVE");
          double result = Double.parseDouble(tokens.get(i+1)) * -1d;
          tokens.set(i+1, String.valueOf(result));
        }
      }
    }

    return removeDoneCalculations(tokens);
  }
  
  public static List<String> calculate(List<String> tokens, int startIndex, int endIndex) {
    printDebug("TOKENS: " + tokens.toString() + "\n");

    for(int i = startIndex;i<endIndex;i++) {
      String token = tokens.get(i);
      
        if("BRACKET_OPEN".equals(token)) {
          for(int j = i;j<endIndex;j++) {
            String innerToken = tokens.get(j);
            
            if("BRACKET_CLOSED".equals(innerToken)) {
              tokens.set(i, "REMOVE");
              tokens.set(j, "REMOVE");
              tokens = removeDoneCalculations(tokens);
              tokens = calculate(tokens, i+1, j-1);
              tokens = removeDoneCalculations(tokens);
              return tokens;
            }
          }
        }
      }

    //Custom User Methods
    for(int i = startIndex;i<endIndex;i++) {
      String token = tokens.get(i);

      if(methods.containsKey(token)) {
        String value = tokens.get(i+1);
        String method = methods.get(token);
        method = method.replace("x", value);
        method = method.replace("X", value);

        List<String> methodTokens = tokenize(method);

        while(methodTokens.size()>1) {
          List<String> prevTokens = methodTokens;
          methodTokens = calculate(methodTokens, 0, methodTokens.size());
          methodTokens = removeDoneCalculations(methodTokens);

          if(methodTokens.equals(prevTokens)) {
            System.out.println("Syntax error");
            methodTokens.set(0, "0");
          }
        }
        if(methodTokens.get(0).equals("NaN")) {
          System.out.println("Math error");
          methodTokens.set(0, "0");
        }
        tokens.set(i, methodTokens.get(0));
        tokens.set(i+1, "REMOVE");
        return tokens;
      }
    }

    //Other
    for(int i = startIndex;i<endIndex;i++) {
      String token = tokens.get(i);

      if("FACTORIAL".equals(token)) {
        double prevToken = Double.parseDouble(tokens.get(i-1));
        for(double j = prevToken-1;j>0;j--) {
          prevToken*=j;
        }

        tokens.set(i, "REMOVE");
        tokens.set(i-1, String.valueOf(prevToken));
        return tokens;
      }
    }
    
      for(int i = startIndex;i<endIndex;i++) {
        String token = tokens.get(i);
      
        if("POWER".equals(token)) {
          double firstNum = Double.parseDouble(tokens.get(i-1));
          double lastNum = Double.parseDouble(tokens.get(i+1));
        
          double result = Math.pow(firstNum, lastNum);
          tokens.set(i, String.valueOf(result));
        
          tokens.set(i-1, "REMOVE");
          tokens.set(i+1, "REMOVE");
          return tokens;
        }
      
        if("SQUARE_ROOT".equals(token)) {
          double num = Double.parseDouble(tokens.get(i+1));
        
          double result = Math.sqrt(num);
          tokens.set(i, "REMOVE");
          tokens.set(i+1, String.valueOf(result));
          return tokens;
        }

        // Sinus, Cosinus, Tangens
        if("SINUS".equals(token)) {
          double num = Double.parseDouble(tokens.get(i+1));

          double result = Math.sin(Math.toRadians(num));
          tokens.set(i, "REMOVE");
          tokens.set(i+1, String.valueOf(result));
          return tokens;
        }
        if("COSINUS".equals(token)) {
          double num = Double.parseDouble(tokens.get(i+1));

          double result = Math.cos(Math.toRadians(num));
          tokens.set(i, "REMOVE");
          tokens.set(i+1, String.valueOf(result));
          return tokens;
        }
        if("TANGENS".equals(token)) {
          double num = Double.parseDouble(tokens.get(i+1));

          double result = Math.tan(Math.toRadians(num));
          tokens.set(i, "REMOVE");
          tokens.set(i+1, String.valueOf(result));
          return tokens;
        }

        // Arcussinus, Arcuscosinus, Arcustangens
        if("ARCUS_SINUS".equals(token)) {
          double num = Double.parseDouble(tokens.get(i+1));

          double result = Math.asin(num)*(180d/Math.PI);
          tokens.set(i, "REMOVE");
          tokens.set(i+1, String.valueOf(result));
          return tokens;
        }
        if("ARCUS_COSINUS".equals(token)) {
          double num = Double.parseDouble(tokens.get(i+1));

          double result = Math.acos(num)*(180d/Math.PI);
          tokens.set(i, "REMOVE");
          tokens.set(i+1, String.valueOf(result));
          return tokens;
        }
        if("ARCUS_TANGENS".equals(token)) {
          double num = Double.parseDouble(tokens.get(i+1));

          double result = Math.atan(num)*(180d/Math.PI);
          tokens.set(i, "REMOVE");
          tokens.set(i+1, String.valueOf(result));
          return tokens;
        }

        //Log
        if(token.contains("LOG")) {
          int base = Integer.parseInt(token.split("_")[1]);
          double number = Double.parseDouble(tokens.get(i+1));

          printDebug("Log base: " + base + " Log value: " + number + "\n");
          printDebug("Result: " + String.valueOf(Math.log(number)/(double)Math.log(base)) + "\n");
          tokens.set(i, String.valueOf(Math.log(number)/(double)Math.log(base)));
          tokens.set(i+1, "REMOVE");

          return tokens;
        }

        //Root
        if(token.contains("ROOT")) {
          int base = Integer.parseInt(token.split("_")[1]);
          double number = Double.parseDouble(tokens.get(i+1));

          tokens.set(i, String.valueOf(Math.pow(number, 1d/((double)base))));
          tokens.set(i+1, "REMOVE");

          return tokens;
        }
    }

    for(int i = startIndex;i<endIndex;i++) {
      String token = tokens.get(i);
      
      if("MULTIPLICATION".equals(token)) {
        double firstNum = Double.parseDouble(tokens.get(i-1));
        double lastNum = Double.parseDouble(tokens.get(i+1));
        
        double result = firstNum * lastNum;
        tokens.set(i, String.valueOf(result));
        
        tokens.set(i-1, "REMOVE");
        tokens.set(i+1, "REMOVE");
        return tokens;
      }
      
      if("DIVISION".equals(token)) {
        double firstNum = Double.parseDouble(tokens.get(i-1));
        double lastNum = Double.parseDouble(tokens.get(i+1));
        
        double result = firstNum / lastNum;
        tokens.set(i, String.valueOf(result));
        
        tokens.set(i-1, "REMOVE");
        tokens.set(i+1, "REMOVE");
        return tokens;
      }
      
      if("MODULO".equals(token)) {
        double firstNum = Double.parseDouble(tokens.get(i-1));
        double lastNum = Double.parseDouble(tokens.get(i+1));
        
        double result = firstNum % lastNum;
        tokens.set(i, String.valueOf(result));
        
        tokens.set(i-1, "REMOVE");
        tokens.set(i+1, "REMOVE");
        return tokens;  
      }

    }
    
    for(int i = startIndex;i<endIndex;i++) {
      String token = tokens.get(i);
    
      if("ADDITION".equals(token)) {
        double firstNum = Double.parseDouble(tokens.get(i-1));
        double lastNum = Double.parseDouble(tokens.get(i+1));
        
        double result = firstNum + lastNum;
        tokens.set(i, String.valueOf(result));
        
        tokens.set(i-1, "REMOVE");
        tokens.set(i+1, "REMOVE");
        return tokens;
      }
      
      if("SUBTRACTION".equals(token)) {
        double firstNum = Double.parseDouble(tokens.get(i-1));
        double lastNum = Double.parseDouble(tokens.get(i+1));
        
        double result = firstNum - lastNum;
        tokens.set(i, String.valueOf(result));
        
        tokens.set(i-1, "REMOVE");
        tokens.set(i+1, "REMOVE");
        return tokens;
      }
    }

    return tokens;
  }
  
  public static List<String> removeDoneCalculations(List<String> tokens) {
    List<String> newTokens = new ArrayList<>();
    
    for (String token : tokens) {
      if(!token.equals("REMOVE")) {
        if(token.length() > 0) {
          newTokens.add(token);
        }
      }
    }

    return newTokens;
  }
  
  public static boolean handleCommand(String str) {
    
    if(str.equals("debug")) {
      debug = !debug;
      System.out.println("Debug turned " + String.valueOf(debug?"on":"off"));
      return true;
    }

    if(str.startsWith("var")) {
      variableCommand(str);
      return true;
    }

    if(str.startsWith("func")) {
      functionCommand(str);
      return true;
    }

    if(str.startsWith("method")) {
      methodCommand(str);
      return true;
    }

    if(str.startsWith("conv")) {
      convertCommand(str);
      return true;
    }

    return false;
  }
  
  public static void printDebug(String content) {
    if(debug) {
      System.out.print(content);
    }
  }

  //COMMANDS
  public static void variableCommand(String str) {
    String[] commandTokens = str.split(" ");

    if(commandTokens.length < 2) {
      System.out.println("Argument for command \'variable\' invalid, did you mean \'set\', \'remove\' or \'remove\'?");
      return;
    }

    switch(commandTokens[1]) {
      case "set": {
        if(illegalVariableNames.contains(commandTokens[2])) {
          System.out.println("Cannot set a variable with this name \"" + commandTokens[2] + "\"");
          return;
        }

        String calculation = "";
        for (int i = 3; i < commandTokens.length; i++) {
          calculation += commandTokens[i];
        }

        List<String> tokens = tokenize(calculation);

        while (tokens.size() > 1) {
          List<String> prevTokens = tokens;
          tokens = calculate(tokens, 0, tokens.size());
          tokens = removeDoneCalculations(tokens);

          if (tokens.equals(prevTokens)) {
            System.out.println("Syntax error");
            return;
          }
          if(tokens.get(0).equals("NaN")) {
            System.out.println("Math error");
            return;
          }
        }

        variables.put(commandTokens[2], Double.parseDouble(tokens.get(0)));
        System.out.println("Set variable \"" + commandTokens[2] + "\" to \'" + Double.parseDouble(tokens.get(0)) + "\'");
        break;
      }
      case "remove": {
        if(!variables.containsKey(commandTokens[2])) {
          System.out.println("Variable \"" + commandTokens[2] + "\" doesn't exist.");
          break;
        }

        System.out.println("Variable \"" + commandTokens[2] + "\" with value \'" + variables.get(commandTokens[2]) + "\' has been removed.");
        variables.remove(commandTokens[2]);

        break;
      }
      case "list": {
        for(String key : variables.keySet()) {
          System.out.println(key + " = " + variables.get(key));
        }
        break;
      }
      default: {
        System.out.println("Argument for command \'variable\' invalid, did you mean \'set\', \'remove\' or \'remove\' instead of \'" + commandTokens[1] + "\'?");
        break;
      }
    }
  }

  public static void functionCommand(String str) {
    System.out.print("\n f(x)=");
    String functionString = input.nextLine();

    System.out.print(" Start value: ");
    double startValue = input.nextDouble();
    System.out.print(" End value: ");
    double endValue = input.nextDouble();
    System.out.print(" Step value: ");
    double stepValue = input.nextDouble();


    //System.out.println("\n f(x)=" + functionString);
    System.out.println("\n\n -------------------------------------");
    System.out.printf(" | %-10s | %-20s |\n", "X", "f(x)");
    System.out.println(" -------------------------------------");

    for(double i = startValue; i <= endValue; i += stepValue) {
      variables.put("x", i);
      variables.put("X", i);
      List<String> tokens = tokenize(functionString);

      while(tokens.size() > 1) {
        List<String> prevTokens = tokens;
        tokens = calculate(tokens, 0, tokens.size());
        tokens = removeDoneCalculations(tokens);

        if(tokens.equals(prevTokens)) {
          System.out.println("Syntax error");
        }
      }

      if(tokens.get(0).equals("NaN")) {
        System.out.println("Math error");
      }

      System.out.printf(" | %-10.2f | %-20s |\n", i, tokens.get(0));

      variables.remove("x");
      variables.remove("X");
    }

    System.out.println(" -------------------------------------");
    return;
  }

  public static void convertCommand(String str) {
    System.out.println(
            "Convertions: \n"+
            " [1] Temperature Convertions\n"+
            " [2] Distance Convertions (WIP)\n"+
            " [3] Time Convertions (WIP)\n"+
            " [4] Data Forms Convertions (WIP)\n"
    );

    System.out.print("Which conversation (1-4): ");

    switch(input.nextInt()) {
      case 1: {
        Conversations.Temperature.handler();
      }
      default: {
        break;
      }
    }
  }

  public static void methodCommand(String str) {
    String[] commandTokens = str.split(" ");

    switch(commandTokens[1]) {
      case "add": {
        System.out.print("Method name: ");
        String methodName = input.nextLine();

        if(illegalVariableNames.contains(methodName) || methodName.length() < 1) {
          System.out.println("Cannot create method with the name \"" + methodName + "\"");
          return;
        }

        System.out.print(methodName + "(x)=");
        String methodEquation = input.nextLine();

        if(methodEquation.length() < 1) {
          return;
        }

        System.out.print("Method created, you can call the method like this \"" + methodName + "(x)\"\nX here by being a random number.");
        methods.put(methodName, methodEquation);
        break;
      }
      case "list": {
        for(String key : methods.keySet()) {
          System.out.println(key + "(x)=" + methods.get(key));
        }
        break;
      }
    }
  }
}

class Conversations {
  public static final Scanner input = new Scanner(System.in);

  static class Temperature {

    public static void handler() {
        System.out.print(
                "\nTemperature Conversions: \n"+
                        " [1] Celsius -> Fahrenheit\n"+
                        " [2] Celsius -> Kelvin\n"+
                        " [3] Fahrenheit -> Celsius\n"+
                        " [4] Fahrenheit -> Kelvin\n"+
                        " [5] Kelvin -> Celsius\n"+
                        " [6] Kelvin -> Fahrenheit\n"
        );

        System.out.print("\nWhich conversation (1-6): ");
        int conversation = input.nextInt();

        System.out.print("\n");

        switch(conversation) {
          case 1: {
            System.out.print("Celsius: ");
            double cTemperature = input.nextDouble();
            double fTemperature = celsiusToFahrenheit(cTemperature);
            System.out.print("Fahrenheit: " + fTemperature);
            break;
          }
          case 2: {
            System.out.print("Celsius: ");
            double cTemperature = input.nextDouble();
            double kTemperature = celsiusToKelvin(cTemperature);
            System.out.print("Kelvin: " + kTemperature);
            break;
          }
          case 3: {
            System.out.print("Fahrenheit: ");
            double fTemperature = input.nextDouble();
            double cTemperature = fahrenheitToCelsius(fTemperature);
            System.out.print("Celsius: " + cTemperature);
            break;
          }
          case 4: {
            System.out.print("Fahrenheit: ");
            double fTemperature = input.nextDouble();
            double kTemperature = fahrenheitToKelvin(fTemperature);
            System.out.print("Kelvin: " + kTemperature);
            break;
          }
          case 5: {
            System.out.print("Kelvin: ");
            double kTemperature = input.nextDouble();
            double cTemperature = kelvinToCelsius(kTemperature);
            System.out.print("Celsius: " + cTemperature);
            break;
          }
          case 6: {
            System.out.print("Kelvin: ");
            double kTemperature = input.nextDouble();
            double fTemperature = kelvinToFahrenheit(kTemperature);
            System.out.print("Fahrenheit: " + fTemperature);
            break;
          }
          default: {
            System.out.println("Invalid option for temperature conversion");
            break;
          }
        }
    }

    //Celsius
    public static double celsiusToFahrenheit(double celsius) {
      return (celsius*9d/5d) + 32d;
    }
    public static  double celsiusToKelvin(double celsius) {
      return celsius+273.15d;
    }

    //Fahrenheit
    public static  double fahrenheitToCelsius(double fahrenheit) {
      return (fahrenheit-32d) * 5d/9d;
    }
    public static  double fahrenheitToKelvin(double fahrenheit) {
      return (fahrenheit-32d) * 5d/9d + 273.15d;
    }

    //Kelvin
    public static  double kelvinToCelsius(double kelvin) {
      return kelvin-273.15d;
    }
    public static  double kelvinToFahrenheit(double kelvin) {
      return (kelvin-273.15d)* 9d/5d + 32d;
    }
  }
}