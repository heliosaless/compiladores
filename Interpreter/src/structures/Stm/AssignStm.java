package structures.Stm;

import structures.Exp.Exp;

public class AssignStm extends Stm{
    public String id; public Exp exp; 
    public AssignStm(String i, Exp e) {id=i; exp=e;}
}