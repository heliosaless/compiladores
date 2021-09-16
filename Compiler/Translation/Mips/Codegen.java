package Translation.Mips;
import Translation.Frame.*;
import Translation.Temp.*;
import Translation.Tree.*;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;


public class Codegen {
    Frame frame;
    ListIterator<Assem.Instr> li;
    
    public Codegen(Frame f, ListIterator<Assem.Instr> li) {
        frame=f;
        this.li=li;
    }
    private Assem.InstrList ilist=null, last=null;
    
    private void emit(Assem.Instr inst){
        if(last != null) last = last.tail = new Assem.InstrList(inst,null);
        else last = ilist = new Assem.InstrList(inst,null);
    }
    
    Assem.InstrList codegen(Stm s){
        Assem.InstrList l;
        munchStm(s);
        l = ilist;
        ilist=last=null;
        return l;
    }
    
    void munchStm(Stm s) {
        if (s instanceof SEQ){          munchStm(((SEQ)s).left); munchStm(((SEQ)s).right); }
        else if (s instanceof LABEL)    munchLabel((LABEL)s);
        else if (s instanceof MOVE)     munchMove(((MOVE)s).dst, ((MOVE)s).src);
        else if (s instanceof JUMP)     munchJump((JUMP)s);
        else if (s instanceof CJUMP)    munchCJump((CJUMP)s);
        else if (s instanceof EXP1 && ((EXP1)s).exp instanceof CALL) munchCall(((CALL)((EXP1)s).exp));
        else{
            System.out.println("ERROR munchStm " + s);
            return;
        }
    }
    
    Translation.Temp.Temp munchExp(Exp s) {
        if (s instanceof MEM)       return munchExpMEM((MEM)s);
        else if(s instanceof BINOP) return munchExpBINOP((BINOP)s);
        else if(s instanceof CONST) return munchExpCONST((CONST)s);
        else if (s instanceof CALL) return munchExpCall(((CALL) s));
        else if(s instanceof NAME) return munchExpNAME((NAME)s);
        else if(s instanceof TEMP)  return ((TEMP)s).temp;
        else{
            System.out.println("ERROR munchEXP " + s );
            return null;
        }
    }
    
    Translation.Temp.Temp munchExpNAME(NAME name){
        return new Temp();
    }
    
    Translation.Temp.Temp munchExpCall(CALL call){
        Temp r = munchExp(call.func);
        TempList l = munchArgs(call.args);

        emit(new Assem.OPER("call `s0\n", tempListConvert(frame.calldefsGetter()), new TempList(r, l)));
        ilist = listInstrConvertToInstrList(frame.procEntryExit2(instrListConvertToListInstr(ilist)));
        ilist = listInstrConvertToInstrList(frame.procEntryExit3(instrListConvertToListInstr(ilist)));
        
        return r;
    }

    Translation.Temp.Temp munchExpCONST(CONST s){
        Temp r = new Temp();
        emit(new Assem.OPER("li `d0, " + s.value + "\n", new TempList(r, null), null));
        return r;
    }
    
    Translation.Temp.Temp munchExpMEM(MEM s){
        if (s.exp instanceof BINOP && ((BINOP) s.exp).binop == BINOP.PLUS
        && ((BINOP) s.exp).right instanceof CONST){
            Temp r = new Temp();
            int i = ((CONST)((BINOP) s.exp).right).value;
            emit(new Assem.OPER("lw `d0, "+ i + "(`s0)\n",
            new TempList(r, null), new TempList(munchExp(((BINOP)s.exp).left), null)));
            return r;
        }
        
        else if (s.exp instanceof BINOP && ((BINOP) s.exp).binop == BINOP.PLUS
        && ((BINOP) s.exp).left instanceof CONST){
            Temp r = new Temp();
            int i = ((CONST)((BINOP) s.exp).left).value;
            emit(new Assem.OPER("lw `d0, "+ i + "(`s0)\n",
            new TempList(r, null), new TempList(munchExp(((BINOP)s.exp).right), null)));
            return r;
        }
        
        else if(s.exp instanceof CONST){
            Temp r = new Temp();
            int i = ((CONST)s.exp).value;
            emit(new Assem.OPER("lw `d0, " + i + "\n", new TempList(r, null), null));
            return r;
        }
        
        else{
            Temp r = new Temp();
            emit(new Assem.OPER("lw `d0, 0(`s0)\n", new TempList(r, null), new TempList(munchExp(s.exp), null)));
            return r;
        }
    }
    
    Translation.Temp.Temp munchExpBINOP(BINOP s){
        if( s.binop == BINOP.PLUS && s.right instanceof CONST){
            Temp r = new Temp(); 
            int i = ((CONST) s.right).value;
            emit(new Assem.OPER("addi `d0, `s0, " + i + "\n", 
            new TempList(r,null), new TempList(munchExp(s.left),null))); 
            return r; 
        }
        
        else if (s.binop == BINOP.PLUS && s.left instanceof CONST) {
            Temp r = new Temp();
            int i = ((CONST) s.left).value;
            emit(new Assem.OPER("addi `d0, `s0, " + i + "\n", new TempList(r, null),
            new TempList(munchExp(s.right), null)));
            return r;
        }
    
        
        else{
            if(s.binop == BINOP.PLUS){
                Temp r = new Temp();
                emit(new Assem.OPER("add `d0,`s0,`s1\n", new TempList(r, null),
                new TempList(munchExp(s.left), new TempList(munchExp(s.right), null))));
                return r;
            }
            else if(s.binop == BINOP.MINUS){
                Temp r = new Temp();
                emit(new Assem.OPER("sub `d0,`s0,`s1\n", new TempList(r, null),
                new TempList(munchExp(s.left), new TempList(munchExp(s.right), null))));
                return r;
            }
            else if (s.binop == BINOP.MUL) {
                Temp r = new Temp();
                emit(new Assem.OPER("mul `d0,`s0,`s1\n", new TempList(r, null),
                new TempList(munchExp(s.left), new TempList(munchExp(s.right), null))));
                return r;
            }
            else if (s.binop == BINOP.DIV) {
                Temp r = new Temp();
                emit(new Assem.OPER("div `d0,`s0,`s1\n", new TempList(r, null),
                new TempList(munchExp(s.left), new TempList(munchExp(s.right), null))));
                return r;
            }
            else{
                System.out.println("ERROR munchExpBINOP " + s + " right " + s.right + " left " + s.left);
                return null;
            }
        }
    }

    void munchCall(CALL call) {
        Temp r = munchExp(call.func);
        TempList l = munchArgs(call.args);

        emit(new Assem.OPER("call `s0\n", tempListConvert(frame.calldefsGetter()), new TempList(r, l)));
        ilist = listInstrConvertToInstrList(frame.procEntryExit2(instrListConvertToListInstr(ilist)));
        ilist = listInstrConvertToInstrList(frame.procEntryExit3(instrListConvertToListInstr(ilist)));
    }

    void munchJump(JUMP s) {
        //Temp aux = munchExp(s.exp);
        emit(new Assem.OPER("j `j0 " + "\n", null, null, s.targets));
    }

    void munchCJump(CJUMP s) {
        if (s.relop == CJUMP.EQ) {
            Temp l = munchExp(s.left);
            Temp r = munchExp(s.right);
            emit(new Assem.OPER("cjump `s0 == `s1 " + s.iffalse + " " + s.iftrue + "\n", null,
                    new TempList(l, new TempList(r, null)), new LabelList(s.iffalse, new LabelList(s.iftrue, null))));
        } else if (s.relop == CJUMP.GE) {
            Temp l = munchExp(s.left);
            Temp r = munchExp(s.right);
            emit(new Assem.OPER("cjump `s0 >= `s1 " + s.iffalse + " " + s.iftrue + "\n", null,
                    new TempList(l, new TempList(r, null)), new LabelList(s.iffalse, new LabelList(s.iftrue, null))));
        } else if (s.relop == CJUMP.GT) {
            Temp l = munchExp(s.left);
            Temp r = munchExp(s.right);
            emit(new Assem.OPER("cjump `s0 > `s1 " + s.iffalse + " " + s.iftrue + "\n", null,
                    new TempList(l, new TempList(r, null)), new LabelList(s.iffalse, new LabelList(s.iftrue, null))));
        } else if (s.relop == CJUMP.LT) {
            Temp l = munchExp(s.left);
            Temp r = munchExp(s.right);
            emit(new Assem.OPER("cjump `s0 < `s1 " + s.iffalse + " " + s.iftrue + "\n", null,
                    new TempList(l, new TempList(r, null)), new LabelList(s.iffalse, new LabelList(s.iftrue, null))));
        } else if (s.relop == CJUMP.NE) {
            Temp l = munchExp(s.left);
            Temp r = munchExp(s.right);
            emit(new Assem.OPER("cjump `s0 != `s1 " + s.iffalse + " " + s.iftrue + "\n", null,
                    new TempList(l, new TempList(r, null)), new LabelList(s.iffalse, new LabelList(s.iftrue, null))));
        } else {
            System.out.println("ERROR munchCJump");
        }
    }
    
    void munchLabel(LABEL s){
        emit(new Assem.LABEL("" + s.label + '\n', s.label));
    }
    
    void munchMove(Exp dst, Exp src) {
        // MOVE(d, e)
        if (dst instanceof MEM) munchMove((MEM)dst,src);
        else if (dst instanceof TEMP) munchMove((TEMP)dst,src);
    }
    
    // MOVE(TEMP(t1), e)
    void munchMove(TEMP dst, Exp src) { 
        if(src instanceof TEMP){
            emit(new Assem.MOVE("move `d0, `s0\n", dst.temp, ((TEMP)src).temp));
        }else{
            emit(new Assem.MOVE("move `d0, `s0\n", 
                dst.temp, munchExp(src)));
            // else emit(new Assem.OPER("addi `d0, `s0, 0\n", 
            //     new TempList(dst.temp,null), new TempList(munchExp(src), null)));
        }
    }
    
    void munchMove(MEM dst, Exp src) {
        // MOVE(MEM(BINOP(PLUS, e1, CONST(i))), e2)
        if (dst.exp instanceof BINOP && ((BINOP)dst.exp).binop==BINOP.PLUS
        && ((BINOP)dst.exp).right instanceof CONST){
            
            Temp dst_ = munchExp(((BINOP)dst.exp).left);
            Temp src_ = munchExp(src);
            
            int i = ((CONST)dst.exp).value;

            
            emit(new Assem.OPER("sw `s1, " + i + "(`s0)\n", null,
            new TempList(dst_,new TempList(src_, null))));
        }
        
        // MOVE(MEM(BINOP(PLUS, CONST(i), e1)), e2)
        else if (dst.exp instanceof BINOP && ((BINOP)dst.exp).binop==BINOP.PLUS
        && ((BINOP)dst.exp).left instanceof CONST){
            
            Temp dst_ = munchExp(((BINOP)dst.exp).right);
            Temp src_ = munchExp(src);
            int i = ((CONST)dst.exp).value;
            
            emit(new Assem.OPER("sw `s1, " + i + "(`s0)\n", null,
            new TempList(dst_,new TempList(src_, null))));
            
        }
          
        else if(dst.exp instanceof CONST){
            Temp src_ = munchExp(src);
            int i = ((CONST)dst.exp).value;
            emit(new Assem.OPER("sw `s0, " + i + "(0)\n", 
            null, new TempList(src_, null)));
        }
        
         
        // MOVE(MEM(e1), e2)
        else{
            Temp dst_ = munchExp(dst.exp);
            Temp src_ = munchExp(src);
            emit(new Assem.OPER("sw `s1, (s0)\n", null,
            new TempList(dst_,new TempList(src_, null))));
        }
        
    }

    TempList munchArgs(ExpList l) {
        int size = l.length;
        Temp[] regs = frame.argRegsGetter();
        TempList regList = null;
        for (int i = 0; i < size; ++i) {
            regList = new TempList(regs[i], regList);

        }
        return regList;
    }

    List<Assem.Instr> instrListConvertToListInstr(Assem.InstrList list){
        List<Assem.Instr> finalList = new LinkedList<Assem.Instr>();
        Assem.InstrList aux = list;
        while(true){
            if(aux == null) break;
            finalList.add(aux.head);
            if(aux.tail == null) break;
            aux = aux.tail;
        }
        return finalList;
    }
    
    Assem.InstrList listInstrConvertToInstrList(List<Assem.Instr> list){
        Assem.InstrList il = null;
        for(int i=list.size()-1; i >= 0; --i){
             il = new Assem.InstrList(list.get(i), il);
             if(i == list.size() - 1) last = il;
        }
    
        return il;
    
    }

    static final TempList tempListConvert(Temp[] arr) {
        if (arr == null)
            return null;
        TempList tl = null;
        for (Temp i : arr) {
            tl = new TempList(i, tl);
        }
        return tl;
    }
    
}





