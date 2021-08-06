package Semantic.Visitors;
import Semantic.SyntaxTree.*;
import Semantic.Symbol.Symbol;
import java.util.HashMap;
import java.util.LinkedList;

public class TypeCheckVisitor implements TypeVisitor {
    ClassDecl currClass;
    MethodDecl currMethod;    

    public HashMap<Symbol, HashMap<Symbol, HashMap<Symbol, Type>>> symbolTable;
    public HashMap<Symbol, HashMap<Symbol, LinkedList<Symbol>>> symbolTableParams;
    public HashMap<Symbol, HashMap<Symbol, Type>> classScope;
    
    public TypeCheckVisitor(HashMap<Symbol, HashMap<Symbol, HashMap<Symbol, Type>>> symbolTable,
            HashMap<Symbol, HashMap<Symbol, Type>> classScope, 
            HashMap<Symbol, HashMap<Symbol, LinkedList<Symbol>>> symbolTableParams) {
        this.classScope = classScope;
        this.symbolTable = symbolTable;
        this.symbolTableParams = symbolTableParams;
    }

    // MainClass m;
    // ClassDeclList cl;
    public Type visit(Program n) {
        n.m.accept(this);
        for (int i = 0; i < n.cl.size(); i++) {
            n.cl.elementAt(i).accept(this);
        }
        return null;
    }

    // Identifier i1,i2;
    // Statement s;
    public Type visit(MainClass n) {
        n.i1.accept(this);
        n.i2.accept(this);
        n.s.accept(this);
        return null;

    }

    // Identifier i;
    // VarDeclList vl;
    // MethodDeclList ml;
    public Type visit(ClassDeclSimple n) {
        currMethod = null;
        currClass = n;
        n.i.accept(this);

        for (int i = 0; i < n.vl.size(); i++) {
            n.vl.elementAt(i).accept(this);
        }
        for (int i = 0; i < n.ml.size(); i++) {
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
        n.i.accept(this);
        n.j.accept(this);

        for (int i = 0; i < n.vl.size(); i++) {
            n.vl.elementAt(i).accept(this);
        }
        for (int i = 0; i < n.ml.size(); i++) {
            n.ml.elementAt(i).accept(this);
        }
        currClass = null;
        return null;
    }

    // Type t;
    // Identifier i;
    public Type visit(VarDecl n) {
        Type varType = n.t.accept(this);
        // String varId = n.i.toString();
        n.i.accept(this);
        /*String idClass = currClass instanceof ClassDeclSimple ? ((ClassDeclSimple) currClass).i.toString()
                : ((ClassDeclExtends) currClass).i.toString();
        HashMap<Symbol, Type> hashClassScope = classScope.get(Symbol.symbol(idClass));
        HashMap<Symbol, Type> hashMethodScope;

        if (currMethod == null) {
            if (hashClassScope.containsKey(Symbol.symbol(varId))) {
                Type expectedType = hashClassScope.get(Symbol.symbol(varId));
                if (!(varType.getClass().equals(expectedType.getClass())))
                    System.out.println("[Type Error] " + varType + " does not match expected type " + expectedType
                            + " for variable " + varId + " on class " + idClass + ".");
            }
        } else {
            hashMethodScope = methodScope.get(Symbol.symbol(currMethod.i.toString()));
            if (hashMethodScope.containsKey(Symbol.symbol(varId))) {
                Type expectedType = hashMethodScope.get(Symbol.symbol(varId));
                if (!(varType.getClass().equals(expectedType.getClass()))) {
                    System.out.println("[Type Error] " + varType + " does not match expected type " + expectedType
                            + " for variable " + varId + " on method " + currMethod.i.toString() + ".");
                }
            }

        }*/

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

        Type expectedType = n.t.accept(this);
        Type returnType = n.e.accept(this);

        if (!(returnType.getClass().equals(expectedType.getClass())))
            System.out.println("[Type Error] Return type " + returnType + " of method " + n.i.toString()
                    + " does not match the expected return type " + expectedType + ".");

        for (int i = 0; i < n.fl.size(); i++) {
            n.fl.elementAt(i).accept(this);
        }
        for (int i = 0; i < n.vl.size(); i++) {
            n.vl.elementAt(i).accept(this);
        }
        for (int i = 0; i < n.sl.size(); i++) {
            n.sl.elementAt(i).accept(this);
        }
        n.e.accept(this);
        currMethod = null;
        return null;
    }

    // Type t;
    // Identifier i;
    public Type visit(Formal n) {
        n.t.accept(this);
        n.i.accept(this);

        return n.t.accept(this);
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
        for (int i = 0; i < n.sl.size(); i++) {
            n.sl.elementAt(i).accept(this);
        }
        return null;
    }

    // Exp e;
    // Statement s1,s2;
    public Type visit(If n) {
        Type a = n.e.accept(this);
        n.s1.accept(this);
        n.s2.accept(this);

        if (a != null && !(a instanceof BooleanType)) {
            System.out.println(
                    "If clouse expects a boolean type as parameter. But received " + a.getClass().getName() + ".");
        }

        return null;
    }

    // Exp e;
    // Statement s;
    public Type visit(While n) {
        Type a = n.e.accept(this);
        n.s.accept(this);
        if (a != null && !(a instanceof BooleanType)) {
            System.out.println(
                    "While loop expects a boolean type as parameter. But received " + a.getClass().getName() + ".");
        }
        return null;
    }

    // Exp e;
    public Type visit(Print n) {
        Type a = n.e.accept(this);
        if (a != null && !(a instanceof IntegerType)) {
            System.out.println(
                    "Print function expects an integer type. But instead received " + a.getClass().getName() + ".");
        }

        return null;
    }

    // Identifier i;
    // Exp e;
    public Type visit(Assign n) {
        Type a = n.i.accept(this);
        Type b = n.e.accept(this);

        if (a != null && b != null && !(a.getClass().equals(b.getClass()))) {
            System.out.println("Invalid assignment for variable " + n.i + " that expects " + a.getClass().getName());
        }

        return null;
    }

    // Identifier i;
    // Exp e1,e2;
    public Type visit(ArrayAssign n) {
        Type a = n.i.accept(this);
        Type b = n.e1.accept(this);
        Type c = n.e2.accept(this);

        if (!(a instanceof IntArrayType)) {
            System.out.println("Identifier " + n.i + " must be of type array.");
        }
        if (!(b instanceof IntegerType)) {
            System.out.println("Invalid array position.");
        }
        if (!(c instanceof IntegerType)) {
            System.out.println("Array must receive integer values.");
        }

        return null;
    }

    // Exp e1,e2;
    public Type visit(And n) {
        Type a = n.e1.accept(this);
        Type b = n.e1.accept(this);

        if (!(a instanceof BooleanType)) {
            System.out.println("Left side of And operator must be a boolean.");
        }
        if (!(b instanceof BooleanType)) {
            System.out.println("Right side of And operator must be a boolean.");
        }

        return new BooleanType();
    }

    // Exp e1,e2;
    public Type visit(LessThan n) {
        Type a = n.e1.accept(this);
        Type b = n.e1.accept(this);

        if (!(a instanceof IntegerType)) {
            System.out.println("Left side of Less Than comparison must be a integer.");
        }
        if (!(b instanceof IntegerType)) {
            System.out.println("Right side of Less Than comparison must be a integer.");
        }

        return new BooleanType();
    }

    public Type visit(Plus n) {
        Type a = n.e1.accept(this);
        Type b = n.e1.accept(this);

        if (!(a instanceof IntegerType)) {
            System.out.println("Left side of addition must be an integer.");
        }
        if (!(b instanceof IntegerType)) {
            System.out.println("Right side of addition must be an integer.");
        }
        return new IntegerType();
    }

    // Exp e1,e2;
    public Type visit(Minus n) {
        Type a = n.e1.accept(this);
        Type b = n.e1.accept(this);

        if (!(a instanceof IntegerType)) {
            System.out.println("Left side of subtraction must be an integer.");
        }
        if (!(b instanceof IntegerType)) {
            System.out.println("Right side of subtraction must be an integer.");
        }
        return new IntegerType();
    }

    // Exp e1,e2;
    public Type visit(Times n) {
        Type a = n.e1.accept(this);
        Type b = n.e1.accept(this);

        if (!(a instanceof IntegerType)) {
            System.out.println("Left side of multiplication must be an integer.");
        }
        if (!(b instanceof IntegerType)) {
            System.out.println("Right side of multiplication must be an integer.");
        }
        return new IntegerType();
    }

    // Exp e1,e2;
    public Type visit(ArrayLookup n) {
        Type a = n.e1.accept(this);
        Type b = n.e2.accept(this);

        if (!(a instanceof IntArrayType)) {
            System.out.println("Type must be an integer array.");
        }
        if (!(b instanceof IntegerType)) {
            System.out.println("Invalid array position.");
        }

        return new IntegerType();
    }

    // Exp e;
    public Type visit(ArrayLength n) {
        Type a = n.e.accept(this);
        if (!(a instanceof IntArrayType)) {
            System.out.println("Invalid length call.");
        }
        return new IntegerType();
    }

    // Exp e;
    // Identifier i;
    // ExpList el;
    public Type visit(Call n) {
        Type a = n.e.accept(this);
        n.i.accept(this);
        if (!(a instanceof IdentifierType)) {
            System.out.println("Invalid method call for method " + n.i + ".");
        }
       // System.out.println(a);

        for (int i = 0; i < n.el.size(); i++) {
            Type b = n.el.elementAt(i).accept(this);
            Type expectedReturn = symbolTable.get(Symbol.symbol(((IdentifierType)a).s)).get(Symbol.symbol(n.i.toString()))
                                    .get(symbolTableParams.get(Symbol.symbol(((IdentifierType)a).s))
                                        .get(Symbol.symbol(n.i.toString())).get(i));
            //System.out.println(symbolTable.get(Symbol.symbol(((IdentifierType)a).s)));
            //System.out.println(symbolTable.get(Symbol.symbol(((IdentifierType)a).s)).get(Symbol.symbol(n.i.toString())));
            //System.out.println(Symbol.symbol(((IdentifierType)a).s)+ " " +Symbol.symbol(n.i.toString()) + " " + expectedReturn);
            if (b != null && !(expectedReturn).getClass().equals(b.getClass())) {
                System.out.println("Invalid type " + b.getClass().getName() + " for method " + n.i + ". "
                        + n.el.elementAt(i).getClass().getName() + " was passed but "
                        + expectedReturn.getClass().getName() + " was Expected.");
            }
        }
        return n.i.accept(this);
    }

    // int i;
    public Type visit(IntegerLiteral n) {
        return new IntegerType();
    }

    public Type visit(True n) {
        return new BooleanType();
    }

    public Type visit(False n) {
        return new BooleanType();
    }

    // String s;
    public Type visit(IdentifierExp n) {
        // try {
        // int num = Integer.parseInt(n.s);
        // System.out.println("Invalid identifier name " + n.s);
        // }
        // catch() {

        // }
        return new Identifier(n.s).accept(this);

    }

    public Type visit(This n) {
        String idClass = currClass instanceof ClassDeclSimple ? ((ClassDeclSimple) currClass).i.toString()
                : ((ClassDeclExtends) currClass).i.toString();
        return new IdentifierType(idClass);
    }

    // Exp e;
    public Type visit(NewArray n) {
        Type a = n.e.accept(this);

        if (!(a instanceof IntegerType)) {
            System.out.println("[Type Error] invalid size " + a.getClass().getName() + " for Array.");
        }
        return new IntArrayType();
    }

    // Identifier i;
    public Type visit(NewObject n) {
        Type a = n.i.accept(this);

        if (a != null && (!(a instanceof IdentifierType))) {
            System.out.println("[Type Error] invalid type " + a.getClass().getName() + " for object" + n.i + ". "
                    + "IdentifierType" + "expected.");
        }

        return new IdentifierType(n.i.toString());
    }

    // Exp e;
    public Type visit(Not n) {
        Type a = n.e.accept(this);

        if (!(a instanceof BooleanType)) {
            System.out.println("[Type Error] invalid type " + a.getClass().getName() + " for Not ");
        }

        return new BooleanType();
    }

    // String s;
    public Type visit(Identifier n) {
        if (currMethod != null)
            return getFromScope(currClass, currMethod.i.toString(), n.toString());
        return getFromScope(currClass, null, n.toString());
        // return null;

    }

    Type getFromScope(ClassDecl class_, String idMethod, String n) {
        HashMap<Symbol, Type> hashMethodScope;

        if (class_ != null) {
            String idClass = class_ instanceof ClassDeclSimple ? ((ClassDeclSimple) class_).i.toString()
                    : ((ClassDeclExtends) class_).i.toString();
            HashMap<Symbol, Type> hashClassScope = classScope.get(Symbol.symbol(idClass));

            if (idMethod == null) {
                return hashClassScope.get(Symbol.symbol(n.toString()));
            } else {
                hashMethodScope = symbolTable.get(Symbol.symbol(idClass)).get(Symbol.symbol(idMethod));
                if (hashMethodScope.get(Symbol.symbol(n.toString())) != null)
                    return hashMethodScope.get(Symbol.symbol(n.toString()));
                else
                    return hashClassScope.get(Symbol.symbol(n.toString()));
            }
        }
/*
        if (idMethod != null) {
            hashMethodScope = symbolTable.get(Symbol.symbol(idClass)).get(Symbol.symbol(idMethod));
            if (hashMethodScope.get(Symbol.symbol(n)) != null)
                return hashMethodScope.get(Symbol.symbol(n));
            else
                return null;
        }*/

        return null;
    }
}