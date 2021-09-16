package Semantic.Visitors;
import Semantic.SyntaxTree.*;
import Semantic.Symbol.Symbol;
import java.util.HashMap;
import java.util.LinkedList;

public class SymbolTableVisitor implements TypeVisitor {
  public ClassDecl currClass;
  public MethodDecl currMethod;
  
  public HashMap<Symbol, HashMap<Symbol, HashMap<Symbol, Type>>> methodScope;
  public HashMap<Symbol, HashMap<Symbol, Type>> classScope;
  public HashMap<Symbol, HashMap<Symbol, LinkedList<Symbol>>> paramScope;


  public SymbolTableVisitor(){
    currClass   = null;
    currMethod  = null;
    methodScope = new HashMap<Symbol, HashMap<Symbol, HashMap<Symbol, Type>>>();
    paramScope = new HashMap<Symbol, HashMap<Symbol, LinkedList<Symbol>>>();
    classScope  = new HashMap<Symbol, HashMap<Symbol, Type>>();

  }

  public HashMap<Symbol, HashMap<Symbol, HashMap<Symbol, Type>>> getMethodScope(){
    return  methodScope;
  }

  public HashMap<Symbol, HashMap<Symbol, LinkedList<Symbol>>> getParams(){
    return paramScope;
  }

  public HashMap<Symbol, HashMap<Symbol, Type>> getClassScope(){
    return classScope;
  }


  // MainClass m;
  // ClassDeclList cl;
  public Type visit(Program n) {
    n.m.accept(this);
    for ( int i = 0; i < n.cl.size(); i++ ) {
        n.cl.elementAt(i).accept(this);
    }
    return null;
  }

  // Identifier i1,i2;
  // Statement s;
  public Type visit(MainClass n) {
    currClass = n;
    n.i1.accept(this);

    String idClass = n.i1.toString();
    if (classScope != null && !classScope.containsKey(Symbol.symbol(idClass))) {
      classScope.put(Symbol.symbol(idClass), new HashMap<Symbol, Type>());
      methodScope.put(Symbol.symbol(idClass), new HashMap<Symbol, HashMap<Symbol, Type>>());
      paramScope.put(Symbol.symbol(idClass), new HashMap<Symbol, LinkedList<Symbol>>());
    }

    
    n.i2.accept(this);
    
    n.s.accept(this);
    currClass = null;
    return null;
  }

  // Identifier i;
  // VarDeclList vl;
  // MethodDeclList ml;
  public Type visit(ClassDeclSimple n) {
    currMethod = null;
    currClass = n;

    String idClass = ((ClassDeclSimple) currClass).i.toString();
    if(classScope != null && !classScope.containsKey(Symbol.symbol(idClass))){
        classScope.put(Symbol.symbol(idClass), new HashMap<Symbol, Type>());
        methodScope.put(Symbol.symbol(idClass), new HashMap<Symbol, HashMap<Symbol, Type>>());
        paramScope.put(Symbol.symbol(idClass), new HashMap<Symbol, LinkedList<Symbol>>());
        //classScope.get(Symbol.symbol(n.i.toString())).put(Symbol.symbol(n.i.toString()), new IdentifierType(n.i.s));

    }

    n.i.accept(this);
    for ( int i = 0; i < n.vl.size(); i++ ) {
        n.vl.elementAt(i).accept(this);
    }
    for ( int i = 0; i < n.ml.size(); i++ ) {
        n.ml.elementAt(i).accept(this);
    }
    currClass = null;
    return null;
  }

  // Identifier i;
  // Identifier j;
  // VarDeclList vl;
  // MethodDeclList ml;
  public Type visit(ClassDeclExtends n) {
    currMethod = null;
    currClass = n;

    String idClass = ((ClassDeclExtends) currClass).i.toString();
    if(classScope != null && !classScope.containsKey(Symbol.symbol(idClass))){
      classScope.put(Symbol.symbol(idClass), new HashMap<Symbol, Type>());
      methodScope.put(Symbol.symbol(idClass), new HashMap<Symbol, HashMap<Symbol, Type>>());
      paramScope.put(Symbol.symbol(idClass), new HashMap<Symbol, LinkedList<Symbol>>());
      //classScope.get(Symbol.symbol(n.i.toString())).put(Symbol.symbol(n.i.toString()), new IdentifierType(n.i.s));

    }
    
    n.j.accept(this);
    n.i.accept(this);
    classScope.get(Symbol.symbol(n.i.toString())).putAll(classScope.get(Symbol.symbol(n.j.toString())));
    methodScope.get(Symbol.symbol(n.i.toString())).putAll(methodScope.get(Symbol.symbol(n.j.toString())));

    for ( int i = 0; i < n.vl.size(); i++ ) {
        n.vl.elementAt(i).accept(this);
    }
    for ( int i = 0; i < n.ml.size(); i++ ) {
        n.ml.elementAt(i).accept(this);
    }
    currClass = null;
    return null;
  }

  // Type t;
  // Identifier i;
  public Type visit(VarDecl n) {

    Type t = n.t.accept(this);
    String id = n.i.toString();

    String idClass = currClass instanceof ClassDeclSimple ?  ((ClassDeclSimple) currClass).i.toString() : ((ClassDeclExtends) currClass).i.toString();
    HashMap<Symbol, Type> hashClassScope;
    HashMap<Symbol, Type> hashMethodScope;

    if(currMethod == null){
      hashClassScope = classScope.get(Symbol.symbol(idClass));
      if(hashClassScope != null && !hashClassScope.containsKey(Symbol.symbol(id))) hashClassScope.put(Symbol.symbol(id), t);
      else  System.out.println(id + " is already defined in " + idClass);

    }else{
      String idMethod = currMethod.i.toString();
      hashMethodScope = methodScope.get(Symbol.symbol(idClass)).get(Symbol.symbol(idMethod));
      if(hashMethodScope != null && !hashMethodScope.containsKey(Symbol.symbol(id))) hashMethodScope.put(Symbol.symbol(id), t);
      else System.out.println(id + " is already defined in " + idClass
                              +  "." + idMethod);
    }

    n.t.accept(this);
    n.i.accept(this);
    return null;
  }

  // Type t;
  // Identifier i;
  // FormalList fl;
  // VarDeclList vl;
  // StatementList sl;
  // Exp e;
  public Type visit(MethodDecl n) {
    currMethod = n;
    String idMethod = currMethod.i.toString();
    String idClass = currClass instanceof ClassDeclSimple ?  ((ClassDeclSimple) currClass).i.toString() : ((ClassDeclExtends) currClass).i.toString();
    
    HashMap<Symbol, Type> hashClassScope = classScope.get(Symbol.symbol(idClass));
    Type methodType = n.t.accept(this);
    n.i.accept(this);


    HashMap<Symbol, HashMap<Symbol, Type>> methodScopeEntry = methodScope.get(Symbol.symbol(idClass));
    HashMap<Symbol, LinkedList<Symbol>> methodParams = paramScope.get(Symbol.symbol(idClass));
    if(methodScopeEntry != null && !methodScopeEntry.containsKey(Symbol.symbol(idMethod))){
        methodScopeEntry.put(Symbol.symbol(idMethod), new HashMap<Symbol, Type>());
        methodParams.put(Symbol.symbol(idMethod), new LinkedList<Symbol>());
        if(hashClassScope != null && !hashClassScope.containsKey(Symbol.symbol(idMethod))) hashClassScope.put(Symbol.symbol(idMethod), methodType);
        else  System.out.println(idMethod + " is already defined in " + idClass);      
    }else{
      System.out.println(idMethod + " is already defined in " + idClass);
    }

    for ( int i = 0; i < n.fl.size(); i++ ) {
        n.fl.elementAt(i).accept(this);
    }
    for ( int i = 0; i < n.vl.size(); i++ ) {
        n.vl.elementAt(i).accept(this);
    }
    for ( int i = 0; i < n.sl.size(); i++ ) {
        n.sl.elementAt(i).accept(this);
    }
    n.e.accept(this);
    currMethod = null;
    return null;
  }

  // Type t;
  // Identifier i;
  public Type visit(Formal n) {
    Type t = n.t.accept(this);
    String id = n.i.toString();
    String idClass = currClass instanceof ClassDeclSimple ?  ((ClassDeclSimple) currClass).i.toString() : ((ClassDeclExtends) currClass).i.toString();
    HashMap<Symbol, Type> hashClassScope;
    HashMap<Symbol, Type> hashMethodScope;
    LinkedList<Symbol> list;
   
    if(currMethod == null){
      hashClassScope = classScope.get(Symbol.symbol(idClass));
      if(hashClassScope != null && !hashClassScope.containsKey(Symbol.symbol(id))) hashClassScope.put(Symbol.symbol(id), t);
      else  System.out.println(id + " is already defined in " + idClass);

    }else{
      String idMethod = currMethod.i.toString();
      hashMethodScope = methodScope.get(Symbol.symbol(idClass)).get(Symbol.symbol(idMethod));
      if(hashMethodScope != null && !hashMethodScope.containsKey(Symbol.symbol(id))){
        hashMethodScope.put(Symbol.symbol(id), t);
        list = paramScope.get(Symbol.symbol(idClass)).get(Symbol.symbol(idMethod));
        if(list!=null)list.add(Symbol.symbol(id));
      }
      else System.out.println(id + " is already defined in " + idClass
                              +  "." + idMethod);
    }


    n.t.accept(this);
    n.i.accept(this);
    return null;
  }

  public Type visit(IntArrayType n) {

    return n;
  }

  public Type visit(BooleanType n) {
    return n;
  }

  public Type visit(IntegerType n) {
    return n;
  }

  // String s;
  public Type visit(IdentifierType n) {
    return n;
  }

  // StatementList sl;
  public Type visit(Block n) {
    for ( int i = 0; i < n.sl.size(); i++ ) {
        n.sl.elementAt(i).accept(this);
    }
    return null;
  }

  // Exp e;
  // Statement s1,s2;
  public Type visit(If n) {
    n.e.accept(this);
    n.s1.accept(this);
    n.s2.accept(this);
    return null;
  }

  // Exp e;
  // Statement s;
  public Type visit(While n) {
    n.e.accept(this);
    n.s.accept(this);
    return null;
  }

  // Exp e;
  public Type visit(Print n) {
    n.e.accept(this);
    return null;
  }

  // Identifier i;
  // Exp e;
  public Type visit(Assign n) {
    n.i.accept(this);
    n.e.accept(this);
    return null;
  }

  // Identifier i;
  // Exp e1,e2;
  public Type visit(ArrayAssign n) {
    n.i.accept(this);
    n.e1.accept(this);
    n.e2.accept(this);
    return null;
  }

  // Exp e1,e2;
  public Type visit(And n) {
    n.e1.accept(this);
    n.e2.accept(this);
    return null;
  }

  // Exp e1,e2;
  public Type visit(LessThan n) {
    n.e1.accept(this);
    n.e2.accept(this);
    return null;
  }

  // Exp e1,e2;
  public Type visit(Plus n) {
    n.e1.accept(this);
    n.e2.accept(this);
    return null;
  }

  


  // Exp e1,e2;
  public Type visit(Minus n) {
    n.e1.accept(this);
    n.e2.accept(this);
    return null;
  }

  // Exp e1,e2;
  public Type visit(Times n) {
    n.e1.accept(this);
    n.e2.accept(this);
    return null;
  }

  // Exp e1,e2;
  public Type visit(ArrayLookup n) {
    n.e1.accept(this);
    n.e2.accept(this);
    return null;
  }

  // Exp e;
  public Type visit(ArrayLength n) {
    n.e.accept(this);
    return null;
  }

  // Exp e;
  // Identifier i;
  // ExpList el;
  public Type visit(Call n) {
    n.e.accept(this);
    n.i.accept(this);
    for ( int i = 0; i < n.el.size(); i++ ) {
        n.el.elementAt(i).accept(this);
    }
    return null;
  }

  // int i;
  public Type visit(IntegerLiteral n) {
    return null;
  }

  public Type visit(True n) {
    return null;
  }

  public Type visit(False n) {
    return null;
  }

  // String s;
  public Type visit(IdentifierExp n) {
    return null;
  }

  public Type visit(This n) {
    return null;
  }

  // Exp e;
  public Type visit(NewArray n) {
    n.e.accept(this);
    return null;
  }

  // Identifier i;
  public Type visit(NewObject n) {
    return null;
  }

  // Exp e;
  public Type visit(Not n) {
    n.e.accept(this);
    return null;
  }

  // String s;
  public Type visit(Identifier n) {
    return null;
  }
}

