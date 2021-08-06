package Translation.Translate;

import Semantic.SyntaxTree.*;
import Semantic.Symbol.Symbol;
import Translation.Frame.Access;
import Translation.Frame.Frame;
import Translation.Temp.Label;
import Translation.Temp.Temp;
import Translation.Tree.*;
import Translation.Tree.ExpList;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class TranslationVisitor implements TypeTranslationVisitor {
  ClassDecl currClass;
  Frame currFrame;
  Frame mainFrame;
  MethodDecl currMethod;
  LinkedList<Frag> fragList;

  public HashMap<Symbol, HashMap<Symbol, HashMap<Symbol, Type>>> methodScope;
  public HashMap<Symbol, HashMap<Symbol, Type>> classScope;

  public HashMap<Symbol, HashMap<Symbol, HashMap<Symbol, Access>>> methodAccess;
  public HashMap<Symbol, HashMap<Symbol, Access>> classAccess;

  public TranslationVisitor(HashMap<Symbol, HashMap<Symbol, HashMap<Symbol, Type>>> methodScope,
      HashMap<Symbol, HashMap<Symbol, Type>> classScope) {

    fragList = new LinkedList<Frag>();
    this.classScope = classScope;
    this.methodScope = methodScope;

    methodAccess = new HashMap<Symbol, HashMap<Symbol, HashMap<Symbol, Access>>>();
    classAccess = new HashMap<Symbol, HashMap<Symbol, Access>>();

    for (Map.Entry<Symbol, HashMap<Symbol, HashMap<Symbol, Type>>> pair1 : methodScope.entrySet()) {
      Symbol classSymbol = pair1.getKey();
      if (!methodAccess.containsKey(classSymbol))
        methodAccess.put(classSymbol, new HashMap<Symbol, HashMap<Symbol, Access>>());
      for (Map.Entry<Symbol, HashMap<Symbol, Type>> pair2 : pair1.getValue().entrySet()) {
        Symbol methodSymbol = pair2.getKey();
        if (!methodAccess.get(classSymbol).containsKey(methodSymbol))
          methodAccess.get(classSymbol).put(methodSymbol, new HashMap<Symbol, Access>());
      }
    }

    for (Map.Entry<Symbol, HashMap<Symbol, Type>> pair1 : classScope.entrySet()) {
      Symbol classSymbol = pair1.getKey();
      if (!classAccess.containsKey(classSymbol))
        classAccess.put(classSymbol, new HashMap<Symbol, Access>());
    }


  }

  public LinkedList<Frag> getFrags() {
    return fragList;
  }

  public void procEntryExit(Stm body, Frame curFrame) {
    Frag fragment = new Frag(body, currFrame);
    fragList.add(fragment);
  }

  // MainClass m;
  // ClassDeclList cl;
  public TranslateExp visit(Program n) {
    n.m.accept(this);
    for (int i = 0; i < n.cl.size(); i++) {
      n.cl.elementAt(i).accept(this);
    }
    return null;
  }

  // Identifier i1,i2;
  // Statement s;
  public TranslateExp visit(MainClass n) {
    currClass = n;
    mainFrame = new Translation.Mips.MipsFrame();

    LinkedList<Boolean> varEscape = new LinkedList<Boolean>();
    varEscape.add(false); varEscape.add(false);
    mainFrame = mainFrame.newFrame(Symbol.symbol("main"), varEscape);
    currFrame = mainFrame;
    

    n.i1.accept(this);
    n.i2.accept(this);    
    n.s.accept(this);
    currClass = null;
    return null;
  }

  // Identifier i;
  // VarDeclList vl;
  // MethodDeclList ml;
  public TranslateExp visit(ClassDeclSimple n) {

    currMethod = null;
    currClass = n;

    for (Map.Entry<Symbol, HashMap<Symbol, Type>> pair1 : classScope.entrySet()) {
      Symbol classSymbol = pair1.getKey();
      for (Map.Entry<Symbol, Type> pair2 : pair1.getValue().entrySet()) {
          Symbol symbol = pair2.getKey();
          Access varAccess = mainFrame.allocLocal(false);
          if(!classAccess.get(classSymbol).containsKey(symbol))
            classAccess.get(classSymbol).put(symbol, varAccess);
      }
    }
    
    n.i.accept(this);
    for (int i = 0; i < n.vl.size(); i++) {
      n.vl.elementAt(i).accept(this);
    }
    for (int i = 0; i < n.ml.size(); i++) {
      n.ml.elementAt(i).accept(this);
    }

    currClass = null;
    currFrame = mainFrame;
    return null;
  }

  // Identifier i;
  // Identifier j;
  // VarDeclList vl;
  // MethodDeclList ml;
  public TranslateExp visit(ClassDeclExtends n) {
    currMethod = null;
    currClass = n;

    for (Map.Entry<Symbol, HashMap<Symbol, Type>> pair1 : classScope.entrySet()) {
      Symbol classSymbol = pair1.getKey();
      for (Map.Entry<Symbol, Type> pair2 : pair1.getValue().entrySet()) {
        Symbol symbol = pair2.getKey();
        Access varAccess = mainFrame.allocLocal(false);
        if (!classAccess.get(classSymbol).containsKey(symbol))
          classAccess.get(classSymbol).put(symbol, varAccess);
      }
    }

    n.i.accept(this);
    n.j.accept(this);
    for (int i = 0; i < n.vl.size(); i++) {
      n.vl.elementAt(i).accept(this);
    }
    for (int i = 0; i < n.ml.size(); i++) {
      n.ml.elementAt(i).accept(this);
    }
    currClass = null;
    currFrame = mainFrame;
    return null;
  }

  // TranslateExp t;
  // Identifier i;
  public TranslateExp visit(VarDecl n) {
    n.t.accept(this);
    n.i.accept(this);
    return null;
  }

  // TranslateExp t;
  // Identifier i;
  // FormalList fl;
  // VarDeclList vl;
  // StatementList sl;
  // Exp e;
  public TranslateExp visit(MethodDecl n) {

    currMethod = n;
    currFrame =  new Translation.Mips.MipsFrame();
    LinkedList<Boolean> varEscape = new LinkedList<Boolean>();

    n.t.accept(this);
    n.i.accept(this);
    varEscape.add(false);
    for (int i = 0; i < n.fl.size(); i++) {
      varEscape.add(false);
      n.fl.elementAt(i).accept(this);
    }

    String idClass = currClass instanceof ClassDeclSimple ? ((ClassDeclSimple) currClass).i.toString()
        : ((ClassDeclExtends) currClass).i.toString();

    currFrame = currFrame.newFrame(Symbol.symbol(idClass+"$"+n.i.toString()), varEscape);

    for (Map.Entry<Symbol, HashMap<Symbol, HashMap<Symbol, Type>>> pair1 : methodScope.entrySet()) {
      Symbol classSymbol = pair1.getKey();
      for (Map.Entry<Symbol, HashMap<Symbol, Type>> pair2 : pair1.getValue().entrySet()) {
        Symbol methodSymbol = pair2.getKey();
        for (Map.Entry<Symbol, Type> pair3 : pair2.getValue().entrySet()) {
          Symbol varSymbol = pair3.getKey();
          Access varAccess = currFrame.allocLocal(false);
          methodAccess.get(classSymbol).get(methodSymbol).put(varSymbol, varAccess);
        }
      }
    }

    for (int i = 0; i < n.vl.size(); i++) {
      n.vl.elementAt(i).accept(this);
    }

    LinkedList<Stm> stmList = new LinkedList<Stm>();
    for (int i = 0; i < n.sl.size(); i++) {
      stmList.add(new EXP1(n.sl.elementAt(i).accept(this).unEx()));
    }
    currFrame.procEntryExit1(stmList); 
    
    
    Stm body = null;
    for (Stm stm : stmList) {
      if(body == null) body = stm;
      body = new SEQ(body, stm);
    }

    TranslateExp returnExp = n.e.accept(this);
    if(body!=null) body = new MOVE(new TEMP(currFrame.RV()), new ESEQ(body, returnExp.unEx()));
    else 
      body = new MOVE(new TEMP(currFrame.RV()), returnExp.unEx());

      
    procEntryExit(body, currFrame);
    currMethod = null;
    currFrame = mainFrame;
    return null;
  }

  // TranslateExp t;
  // Identifier i;
  public TranslateExp visit(Formal n) {
    n.t.accept(this);
    n.i.accept(this);
    return null;
  }

  public TranslateExp visit(IntArrayType n) {
    return null;
  }

  public TranslateExp visit(BooleanType n) {
    return null;
  }

  public TranslateExp visit(IntegerType n) {
    return null;
  }

  // String s;
  public TranslateExp visit(IdentifierType n) {
    return null;
  }

  // StatementList sl;
  public TranslateExp visit(Block n) {
    Stm seq = null;
    for (int i = 0; i < n.sl.size(); i++) {
      if(i == 0) seq = new EXP1(n.sl.elementAt(i).accept(this).unEx());
      else seq = new SEQ(seq, new EXP1(n.sl.elementAt(i).accept(this).unEx()));
    }
    return new TranslateExp(new ESEQ(seq, new CONST(0)));
  }

  // Exp e;
  // Statement s1,s2;
  public TranslateExp visit(If n) {
    TranslateExp exp = n.e.accept(this);
    Stm stm1 = new EXP1(n.s1.accept(this).unEx());
    Stm stm2 = new EXP1(n.s2.accept(this).unEx());

    Translation.Temp.Label t = new Translation.Temp.Label();
    Translation.Temp.Label f = new Translation.Temp.Label();

    SEQ trueStm = new SEQ(new LABEL(t), stm1);
    SEQ falseStm = new SEQ(new LABEL(f), stm2);

    CJUMP cjump = new CJUMP(CJUMP.EQ, exp.unEx(), new CONST(1), t, f);


    SEQ ifStm = new SEQ(cjump, new SEQ(trueStm, falseStm));

    return new TranslateExp(new ESEQ(ifStm, new CONST(0)));
  }

  // Exp e;
  // Statement s;
  public TranslateExp visit(While n) {
    Translation.Tree.Exp exp = n.e.accept(this).unEx();
    Stm stm = new EXP1(n.s.accept(this).unEx());

    Translation.Temp.Label done = new Translation.Temp.Label();
    Translation.Temp.Label body = new Translation.Temp.Label();
    Translation.Temp.Label test = new Translation.Temp.Label();

    CJUMP cjump = new CJUMP(CJUMP.EQ, exp, new CONST(1), body, done);
    JUMP testJump = new JUMP(test);
    
    SEQ whileStm = new SEQ(new LABEL(test), new SEQ(cjump, new SEQ(new LABEL(body), new SEQ(stm, new SEQ(testJump, new LABEL(done))))));

    return new TranslateExp(new ESEQ(whileStm, new CONST(0)));

  }

  // Exp e;
  public TranslateExp visit(Semantic.SyntaxTree.Print n) {
    Translation.Tree.Exp ex = currFrame.externalCall("print",
        new Translation.Tree.ExpList(n.e.accept(this).unEx(), null));
    return new TranslateExp(ex);
  }

  // Identifier i;
  // Exp e;
  public TranslateExp visit(Assign n) {
    n.i.accept(this);
    TranslateExp v = n.e.accept(this);
    Translation.Tree.Exp a = getAccess(Symbol.symbol(n.i.s));

    MOVE move;
    if(n.e instanceof NewObject){
      move = new MOVE(a,a);
    }else{
      move = new MOVE(a, v.unEx());
    }

    return new TranslateExp(new ESEQ(move, new CONST(0)));
  }

  // Identifier i;
  // Exp e1,e2;
  public TranslateExp visit(ArrayAssign n) {
    TranslateExp id = n.i.accept(this);
    TranslateExp position = n.e1.accept(this);
    TranslateExp value = n.e2.accept(this);

    Translation.Tree.Exp vector = getAccess(Symbol.symbol(n.i.toString()));

    MOVE arrStm = new MOVE(
      new BINOP(BINOP.PLUS, new BINOP(BINOP.MUL, position.unEx(), new CONST(currFrame.wordSize())), new MEM(vector)),
            value.unEx());

    return new TranslateExp(new ESEQ(arrStm, new CONST(0)));
  }

  // Exp e1,e2;
  public TranslateExp visit(And n) {
    TranslateExp e1 = n.e1.accept(this);
    TranslateExp e2 = n.e2.accept(this);
    
    Translation.Temp.Temp r = new Translation.Temp.Temp();
    Label t = new Label();
    Label f = new Label();
    Label join = new Label();


    CJUMP cond1 = new CJUMP(CJUMP.EQ, e1.unEx(), new CONST(1), t, f);
    CJUMP cond2 = new CJUMP(CJUMP.EQ, e2.unEx(), new CONST(1), join, f);
    MOVE  ret1 = new MOVE(new TEMP(r), new CONST(1));
    MOVE  ret0 = new MOVE(new TEMP(r), new CONST(0));


    ESEQ andExp = new ESEQ(
    new SEQ(ret1, 
      new SEQ(cond1, 
        new SEQ(new LABEL(t),
          new SEQ(cond2, 
            new SEQ(new LABEL(f),
              new SEQ(ret0, new LABEL(join))))))),
    new TEMP(r));


    return new TranslateExp(andExp);
  }

  // Exp e1,e2;
  public TranslateExp visit(LessThan n) {
    TranslateExp e1 = n.e1.accept(this);
    TranslateExp e2 = n.e2.accept(this);
    Translation.Temp.Temp r = new Translation.Temp.Temp();
    Label t = new Label();
    Label f = new Label();

    CJUMP unCx = new CJUMP(CJUMP.LT, e1.unEx(), e2.unEx(), t, f);
    ESEQ lt = new ESEQ(
                  new SEQ(
                        new MOVE(new TEMP(r), new CONST(1)),
                        new SEQ(unCx, new SEQ(new LABEL(f), new SEQ(new MOVE(new TEMP(r), new CONST(0)), new LABEL(t))))
                        ), new TEMP(r));
    return new TranslateExp(lt);
        
  }

  // Exp e1,e2;
  public TranslateExp visit(Plus n) {
    TranslateExp e1 = n.e1.accept(this);
    TranslateExp e2 = n.e2.accept(this);

    BINOP binop = new BINOP(BINOP.PLUS, e1.unEx(), e2.unEx());
    return new TranslateExp(binop);
  }

  // Exp e1,e2;
  public TranslateExp visit(Minus n) {
    TranslateExp e1 = n.e1.accept(this);
    TranslateExp e2 = n.e2.accept(this);

    BINOP binop = new BINOP(BINOP.MINUS, e1.unEx(), e2.unEx());
    return new TranslateExp(binop);
  }

  // Exp e1,e2;
  public TranslateExp visit(Times n) {
    TranslateExp e1 = n.e1.accept(this);
    TranslateExp e2 = n.e2.accept(this);

    BINOP binop = new BINOP(BINOP.MUL, e1.unEx(), e2.unEx());
    return new TranslateExp(binop);
  }

  // Exp e1,e2;
  public TranslateExp visit(ArrayLookup n) {
    n.e1.accept(this);

    Translation.Tree.Exp arr = getAccess(Symbol.symbol(((IdentifierExp) n.e1).s));
    TranslateExp position = n.e2.accept(this);

    return new TranslateExp(
        new BINOP(BINOP.PLUS, new BINOP(BINOP.MUL, position.unEx(), new CONST(currFrame.wordSize())), new MEM(arr)));
  }

  // Exp e;
  public TranslateExp visit(ArrayLength n) {
    Translation.Tree.Exp arr = getAccess(Symbol.symbol(((IdentifierExp)n.e).s));
    return new TranslateExp(arr);
  }

  // later
  // Exp e;
  // Identifier i;
  // ExpList el;
  public TranslateExp visit(Call n) {
   n.e.accept(this);
   n.i.accept(this);
   Translation.Tree.Exp p;

   if(n.e instanceof This || n.e instanceof Call || n.e instanceof NewObject){
     Temp temp = new Temp();
     p = new TEMP(temp);
   }else{
     p = getAccess(Symbol.symbol(((IdentifierExp)n.e).s));
   }

    ExpList args = new ExpList(p, null);
    for (int i = 0; i < n.el.size(); i++) {
      TranslateExp arg = n.el.elementAt(i).accept(this);
      args = new ExpList(arg.unEx(), args);
    }

    Label funcLabel = new Label();
    return new TranslateExp(new CALL(new NAME(funcLabel), args));
  }

  // int i;
  public TranslateExp visit(IntegerLiteral n) {
    return new TranslateExp(new CONST(n.i));
  }

  public TranslateExp visit(True n) {
    return new TranslateExp(new CONST(1));
  }

  public TranslateExp visit(False n) {
    return new TranslateExp(new CONST(0));
  }

  // String s;
  public TranslateExp visit(IdentifierExp n) {
      return new TranslateExp(getAccess(Symbol.symbol(n.s)));
  }

  public TranslateExp visit(This n) {
    return new TranslateExp(new TEMP(new Temp()));
  }

  // Exp e;
  public TranslateExp visit(NewArray n) {
    TranslateExp size_ = n.e.accept(this);
    Translation.Tree.Exp ia = currFrame.externalCall("initArray", new ExpList(size_.unEx(), new ExpList(new CONST(0), null)));
    return new TranslateExp(ia);
  }

  // Identifier i;
  public TranslateExp visit(NewObject n) {
    return new TranslateExp(new CONST(0));
  }

  // Exp e;
  public TranslateExp visit(Not n) {
    BINOP binop = new BINOP(BINOP.MINUS, new CONST(1), n.e.accept(this).unEx());
    return new TranslateExp(binop);
  }

  // String s;
  public TranslateExp visit(Identifier n) {
    return null;
  }

  public Translation.Tree.Exp getAccess(Symbol n){
    if(currMethod==null || currClass==null || currFrame ==null) System.out.println("VEr depois " + n);
    else{
      String idClass = currClass instanceof ClassDeclSimple ? ((ClassDeclSimple) currClass).i.toString()
          : ((ClassDeclExtends) currClass).i.toString();

      Translation.Tree.Exp a;

      if(methodAccess.get(Symbol.symbol(idClass)).get(Symbol.symbol(currMethod.i.toString())).containsKey(n)) 
      {
        a = methodAccess.get(Symbol.symbol(idClass)).get(Symbol.symbol(currMethod.i.toString())).get(n).exp(new TEMP(currFrame.FP()));
      }else{
        a = classAccess.get(Symbol.symbol(idClass)).get(n).exp(new TEMP(currFrame.FP()));
      }

      if(a == null) System.out.println("symbol " + n + " not found." );
      return a;
    }
    return null;
  }
}