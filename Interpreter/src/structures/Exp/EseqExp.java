package structures.Exp;

import structures.Stm.*;

public class EseqExp extends Exp { 
    public Stm stm; public Exp exp; 
    public EseqExp(Stm s, Exp e) {stm=s; exp=e;}

}