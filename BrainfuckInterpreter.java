public class BrainfuckInterpreter {

    private static final Scanner in = new Scanner(System.in);

    public static void main(String[] args) {
        interpretate("++++++++[>++++[>++>+++>+++>+<<<<-]>+>+>->>+[<]<-]>>.>---.+++++++..+++.>>.<-.<.+++.------.--------.>>+.>++.");
    }

    public static void interpretate(String code) {
        int pointer = 0;
        byte[] cells = new byte[30_000];

        for(int programCounter = 0;programCounter<code.length();programCounter++) {
            char c = code.charAt(programCounter);

            switch(c) {
                case '<' -> pointer--;
                case '>' -> pointer++;
                case '+' -> cells[pointer]++;
                case '-' -> cells[pointer]--;
                case '.' -> print(cells[pointer]);
                case ',' -> cells[pointer] = getInput();
                case '[' -> {
                    if(cells[pointer] == 0) {
                        int indentCounter = 1;

                        for(int i = programCounter + 1;i<code.length();i++) {
                            char innerChar = code.charAt(i);

                            if(innerChar == ']') indentCounter--;
                            else if (innerChar == '[') indentCounter++;

                            if(indentCounter == 0) {
                                programCounter = i;
                                break;
                            }
                        }
                    }
                }
                case ']' -> {
                    if(cells[pointer] != 0) {
                        int indentCounter = 1;

                        for(int i = programCounter - 1;i>0;i--) {
                            char innerChar = code.charAt(i);

                            if(innerChar == ']') indentCounter++;
                            else if(innerChar == '[') indentCounter--;

                            if(indentCounter == 0) {
                                programCounter = i;
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    public static void print(byte value) {
        System.out.print((char)Byte.toUnsignedInt(value));
    }

    public static byte getInput() {
        return (byte)in.next().charAt(0);
    }
}
