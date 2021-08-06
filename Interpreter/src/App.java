import structures.Stm.*;
import structures.Exp.*;
import structures.ExpList.*;
import java.util.Map;
import java.util.HashMap;

public class App {

    static int interpExp(Exp exp, Map<String,Integer> table){
        if(exp instanceof IdExp){
            if( table.containsKey(((IdExp)exp).id) ) return table.get(((IdExp)exp).id); //Se não existir não foi inicializado. 
            else {
                System.out.println("Variável não inicializada");
                System.exit(1);
                return 0;
            }
        }

        else if(exp instanceof NumExp){
            return ((NumExp)exp).num;
        }

        else if(exp instanceof OpExp){
            if(((OpExp)exp).oper==OpExp.Plus)return interpExp(((OpExp)exp).left, table) + interpExp(((OpExp)exp).right, table); 
            else if(((OpExp)exp).oper==OpExp.Minus)return interpExp(((OpExp)exp).left, table) - interpExp(((OpExp)exp).right, table); 
            else if(((OpExp)exp).oper==OpExp.Times)return interpExp(((OpExp)exp).left, table) * interpExp(((OpExp)exp).right, table); 
            else if(((OpExp)exp).oper==OpExp.Div)return interpExp(((OpExp)exp).left, table) / interpExp(((OpExp)exp).right, table); 
            else{ 
                System.out.println("Escolha uma operação válida.");
                System.exit(1);
                return 0;
            }
        }

        else if(exp instanceof EseqExp){
            interpStm(((EseqExp)exp).stm, table);
            return interpExp(((EseqExp)exp).exp, table);
        }
        
        else{
            System.out.println("Não existe tal subclasse de Exp");
            System.exit(1); 
            return 0;
        }
    }

    static void interpStm(Stm stm, Map<String, Integer> table){
        if(stm instanceof AssignStm){
            AssignStm new_stm = ((AssignStm)stm);
            String id = new_stm.id;
            Exp exp = new_stm.exp;
            table.put(id, interpExp(exp, table));

        }else if(stm instanceof PrintStm){
            ExpList exps = ((PrintStm)stm).exps;

            while(exps instanceof PairExpList){
                System.out.print(App.interpExp(((PairExpList)exps).head, table) + " ");
                exps = ((PairExpList)exps).tail;
            }
            System.out.print(App.interpExp(((LastExpList)exps).head, table) + " ");
            System.out.println("");
        }
        
        else if(stm instanceof CompoundStm){
            CompoundStm new_stm = ((CompoundStm)stm);
            App.interpStm(new_stm.stm1, table);
            App.interpStm(new_stm.stm2, table);
        }
    
        else{
            System.out.println("Não existe tal subclasse de Stm");
            System.exit(1); 
            return;
        }
    }


    public static void main(String[] args) throws Exception {
        Map<String, Integer> table = new HashMap<>();
        
        Stm prog = new CompoundStm( 
                    new AssignStm("a", new OpExp(new NumExp(5), OpExp.Plus, new NumExp(3))), 
                    new CompoundStm( 
                        new AssignStm("b", new EseqExp(new PrintStm(
                            new PairExpList(new IdExp("a"), new LastExpList(new OpExp(new IdExp("a"),OpExp.Minus,new NumExp(1))))),
                            new OpExp(new NumExp(10), OpExp.Times, new IdExp("a")))), 
                        new PrintStm(new LastExpList(new IdExp("b"))))); 

        interpStm(prog, table);
        
        System.out.println("\nTABELA DE SIMBOLOS:");
        for (String key : table.keySet()) {
            Integer value = table.get(key);
            System.out.println(key + " = " + value);
        }

    }
}
