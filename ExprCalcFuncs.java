import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

public class ExprCalcFuncs{
    public static HashMap<String,Integer> operationPriority  = new HashMap<>();
    static {
        operationPriority.put("(", 0);
        operationPriority.put(")", 0);
        operationPriority.put("+", 1);
        operationPriority.put("-", 1);
        operationPriority.put("*", 2);
        operationPriority.put("/", 2);
        operationPriority.put("^", 3);
        operationPriority.put("~", 4);
    }
    public static final Set<String> operations = operationPriority.keySet();

    public static HashMap<String, BinaryOperation> opsDict = new HashMap<>();
    static {
        opsDict.put("+", Double::sum);
        opsDict.put("-", (a,b) -> a-b);
        opsDict.put("*", (a,b) -> a*b);
        opsDict.put("/", (a,b) -> a/b);
        opsDict.put("^", Math::pow);
    }

    /*
    Преобразует выражение в инфиксной нотации в постфиксную нотацию
    */
    public static String infixToPostfix(String infix){
        LinkedList<String> stack = new LinkedList<>();
        LinkedList<String> postfix = new LinkedList<>();
        infix = normaliseExpr(infix);
        boolean start = true;
        for(String term: infix.split(" ")){
            if (Character.isDigit(term.charAt(0))) postfix.add(term);
            else if (Character.isAlphabetic(term.charAt(0))) postfix.add(term);
            else if (term.equals("(")){
                start = true;
                stack.addFirst("(");
                continue;
            }
            else if (term.equals(")")){
                if (start) return "Error";
                while (!stack.isEmpty() && !stack.getFirst().equals("(")){
                    postfix.add(stack.pop());
                }
                if (stack.isEmpty()) return "Error";
                else stack.removeFirst();
            }
            else if (operations.contains(term)){
                if(start){
                    if (term.equals("-")) postfix.add("0.0");
                    else return "Error";
                }
                else {
                    while (!stack.isEmpty() &&
                            operationPriority.get(stack.getFirst())
                                    >= operationPriority.get(term)) {
                        postfix.add(stack.pop());
                    }
                }
                stack.addFirst(term);
            }
            else return "Error";
            start = false;
        }
        postfix.addAll(stack);
        return String.join(" ", postfix);
    }

    /*
    Все операторы и скобки обрамляются пробелом. Буквы приводятся к верхнему регистру.
    Пример: (a1+4)*b2 → ( A1 + 4 ) * B2
    */
    public static String normaliseExpr(String input){
        String res = input.toUpperCase();
        res = res.replaceAll(" ", "");
        for (String term: operations) {
            res = res.replaceAll("\\%s".formatted(term), " %s ".formatted(term));
        }
        res = res.replaceAll("  ", " ");
        res = res.trim();
        return res;
    }


    /*
    Вычисляет формулу, записанную в обратной польской нотации.
     */
    public static Double calcPostfixExpr(String input, HashMap<String, Double> dict) throws IncorrectExpr{
        try {
            if (input.equals("Error")) throw new IncorrectExpr();
            ;
            LinkedList<Double> stack = new LinkedList<>();
            for (String term : input.split(" ")) {
                if (Character.isDigit(term.charAt(0))) {
                    try {
                        stack.addFirst(Double.parseDouble(term));
                    } catch (NumberFormatException nfe) {
                        throw new IncorrectExpr();
                    }
                } else if (Character.isAlphabetic(term.charAt(0))) {
                    stack.addFirst(dict.get(term));
                } else if (operations.contains(term)) {
                    Double b = stack.pop();
                    Double a = stack.pop();
                    stack.addFirst(opsDict.get(term).eval(a, b));
                } else throw new IncorrectExpr();
                ;
            }
            return stack.pop();
        }
        catch (Throwable thr){
            throw new IncorrectExpr();
        }
    }

    public static void main(String[] args) {
        String expr = "-(-A1)^2.0 +2^3^2";
        expr = "2^(3+2)^2 +2.1*A1";
        HashMap<String, Double> dict = new HashMap<>();
        dict.put("A1", 2.0);
        System.out.println(expr);
        System.out.println(normaliseExpr(expr));
        System.out.println(infixToPostfix(expr));
        Double value = calcPostfixExpr(infixToPostfix(expr), dict);
        System.out.println(value);
        System.out.println('A' == 65);
    }
}