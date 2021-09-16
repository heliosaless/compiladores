package Translation.Frame;
import java.util.List;
import java.util.HashMap;

public abstract class Frame implements Translation.Temp.TempMap {
    public Translation.Temp.Label name;
    public List<Access> formals;
    public abstract Frame newFrame(Semantic.Symbol.Symbol name, List<Boolean> formals);
    public abstract Access allocLocal(boolean escape);
    public abstract Translation.Temp.Temp FP();
    public abstract int wordSize();
    public abstract Translation.Tree.Exp externalCall(String func, Translation.Tree.ExpList args);
    public abstract Translation.Temp.Temp RV();
    public abstract String string(Translation.Temp.Label label, String value);
    public abstract Translation.Temp.Label badPtr();
    public abstract Translation.Temp.Label badSub();
    public abstract String tempMap(Translation.Temp.Temp temp);

    public abstract List<Assem.Instr> codegen(List<Translation.Tree.Stm> stms);
    // public abstract Assem.InstrList codegen(Translation.Tree.Stm stms);
    public abstract void procEntryExit1(List<Translation.Tree.Stm> body);
    public abstract List<Assem.Instr> procEntryExit2(List<Assem.Instr> body);
    public abstract List<Assem.Instr> procEntryExit3(List<Assem.Instr> body);
    public abstract Translation.Temp.Temp[] registers();
    // public abstract void spill(List<Assem.Instr> insns, Translation.Temp.Temp[] spills);
    public abstract String programTail(); //append to end of target code
    public abstract Translation.Temp.Temp[] argRegsGetter();
    public abstract Translation.Temp.Temp[] calldefsGetter();
    public abstract Translation.Temp.Temp[] returnSinkGetter();
    public abstract HashMap<Translation.Temp.Temp,String> tempMapGetter();
}