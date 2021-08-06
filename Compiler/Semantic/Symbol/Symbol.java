package Semantic.Symbol;
import java.util.HashMap;
import java.util.Map;
public class Symbol {
    private String name;
    private Symbol(String n) {name=n; }
    private static Map<String,Symbol> dict = new HashMap<String,Symbol>();
    public String toString() {return name;}
    public static Symbol symbol(String n) {
        String u = n.intern();
        Symbol s = null;
        if (!dict.containsKey(u)) {
            s = new Symbol(u);
            dict.put(u,s);
        }
        
        return dict.get(u);
    }


}