import java.util.Map;
import java.util.HashMap;
import java.util.Stack;
import java.util.Vector;

class LR{
    private Map<Integer, Map<String, String>> dfa; 
    private Stack<String> stack;
    private Vector<String> rules;

    public LR(){
        stack = new Stack<>();

        dfa = new HashMap<>();
        rules = new Vector<>();

        rules.add("S = S ; S");
        rules.add("S = id = E");
        rules.add("S = print ( L )");
        rules.add("E = id");
        rules.add("E = num");
        rules.add("E = E + E");
        rules.add("E = ( S , E) ");
        rules.add("L = E");
        rules.add("L = L , E");

        Map<String, String> dfa0 = new HashMap<>(); //...
        dfa0.put("S", "goto 1");
        dfa0.put("id", "shift 2");
        dfa0.put("print", "shift 3");

        Map<String, String> dfa1 = new HashMap<>(); // ...
        dfa1.put(";", "shift 4");
        dfa1.put("$", "accept");

        Map<String, String> dfa2 = new HashMap<>(); // ...
        dfa2.put("=", "shift 5");

        Map<String, String> dfa3 = new HashMap<>(); // ...
        dfa3.put("(", "shift 6");
        
        Map<String, String> dfa4 = new HashMap<>(); // ...
        dfa4.put("S", "goto 7");
        dfa4.put("S", "reduce 0");
        dfa4.put("print", "shift 3");
        dfa4.put("id", "shift 2");

        Map<String, String> dfa5 = new HashMap<>(); // ...
        dfa5.put("E", "goto 8");
        dfa5.put("E", "reduce 1");
        dfa5.put("(", "shift 9");
        dfa5.put("id", "reduce 3");
        dfa5.put("num", "reduce 4");
        
        Map<String, String> dfa6 = new HashMap<>(); // ...
        dfa6.put("E", "goto 11");
        dfa6.put("E", "reduce 7");
        dfa6.put("L", "goto 10");
        dfa6.put("(", "shift 9");
        dfa6.put("id", "reduce 3");
        dfa6.put("num", "reduce 4");

        Map<String, String> dfa7 = new HashMap<>(); // ...
        dfa7.put(";", "shift 4");

        Map<String, String> dfa8 = new HashMap<>(); // ...
        dfa8.put("+", "shift 12");

        Map<String, String> dfa9 = new HashMap<>(); // ...
        dfa9.put("id", "shift 2"); 
        dfa9.put("print", "shift 3"); 
        dfa9.put("S", "goto 13"); 

        Map<String, String> dfa10 = new HashMap<>(); // ...
        dfa10.put(")", "reduce 2");
        dfa10.put(",", "shift 15");

        Map<String, String> dfa11 = new HashMap<>(); // ...
        dfa11.put("+", "shift 12");

        Map<String, String> dfa12 = new HashMap<>(); // ...
        dfa12.put("E", "reduce 5");
        dfa12.put("E", "goto 16");
        dfa12.put("(", "shift 9");

        Map<String, String> dfa13 = new HashMap<>(); // ...
        dfa13.put(";", "shift 4");
        dfa13.put(",", "shift 17");

        Map<String, String> dfa14 = new HashMap<>(); // ...

        Map<String, String> dfa15 = new HashMap<>(); // ...
        dfa15.put("id", "reduce 3");
        dfa15.put("num", "reduce 4");
        dfa15.put("(", "shift 9");
        dfa15.put("E", "reduce 8");

        Map<String, String> dfa16 = new HashMap<>(); // ...
        dfa16.put("+", "shift 12");

        Map<String, String> dfa17 = new HashMap<>(); // ...
        dfa17.put("id", "reduce 3");
        dfa17.put("num", "reduce 4");
        dfa17.put("E", "goto 19");
        dfa17.put("(", "shift 9");

        Map<String, String> dfa18 = new HashMap<>(); // ...
        dfa18.put("+", "shift 12");

        Map<String, String> dfa19 = new HashMap<>(); // ...
        dfa19.put(")", "reduce 6");
        dfa19.put("+", "shift 12");

        dfa.put(0, dfa0);
        dfa.put(1, dfa1);
        dfa.put(2, dfa2);
        dfa.put(3, dfa3);
        dfa.put(4, dfa4);
        dfa.put(5, dfa5);
        dfa.put(6, dfa6);
        dfa.put(7, dfa7);
        dfa.put(8, dfa8);
        dfa.put(9, dfa9);
        dfa.put(10, dfa10);
        dfa.put(11, dfa11);
        dfa.put(12, dfa12);
        dfa.put(13, dfa13);
        dfa.put(14, dfa14);
        dfa.put(15, dfa15);
        dfa.put(16, dfa16);
        dfa.put(17, dfa17);
        dfa.put(18, dfa18);
        dfa.put(19, dfa19);

    }

    private Integer shift_(Integer state, String value) {
        stack.push(value);
        return Integer.parseInt(value.split(" ")[1]);
    }

    private Integer reduce_(Integer state, Integer rule_number) {
        String rule = rules.get(rule_number);
        System.out.println(rule);
        Integer before_state = null;

        String top_stack = stack.pop().split(" ")[0];
        Boolean first = true;

        while(!top_stack.equals(rule.split(" ")[2])){
            top_stack = stack.pop();
            if(first){
                before_state = Integer.parseInt(top_stack.split(" ")[1]);
                first = false;
            }
            top_stack = top_stack.split(" ")[0];
        }
        stack.pop();

        stack.push(rule.split(" ")[0]);
        return Integer.parseInt(dfa.get(before_state).get(stack.lastElement()));
    }


    public Boolean exec(String input) {
        stack.clear();
        Integer current_state = 0;
        String[] tokens = input.split(" ");

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            Integer rule;

            if (token.matches("\\d+"))
                token = "num";
            else {
                if (token.matches("\\w+"))
                    token = "id";
            }

            String value = dfa.get(current_state).get(token);
            if(stack.isEmpty() || value.split(" ")[0] == "shift") current_state = shift_(current_state, value);
            else if(value.split(" ")[0] == "reduce"){
                rule = Integer.parseInt(value.split(" ")[1]);
                current_state = reduce_(current_state, rule);
                i = i - 1;
            }
            else if(value.split(" ")[0] == "accept") return true;
            else return false;
        }
    
        return false;
    }

}


public class Main {

    public static void main(String[] args) {
        String input = "a = 87 ; b = c + ( d = 5 + 6 , d ) $";


        LR parser = new LR();
        parser.exec(input);

    }    
}
