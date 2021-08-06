import Semantic.SyntaxTree.*;
import Semantic.Symbol.*;
import Semantic.Visitors.*;
import Translation.Frame.Access;
import Translation.Translate.*;
import Translation.Tree.*;
import Lex.*;
import java. util.Iterator;
import java. util.Map;

import Canon.BasicBlocks;
import Canon.TraceSchedule;

import java.io.PrintStream;
import java. util.HashMap;
import java. util.LinkedList;



public class Main {

   public static void printScope(HashMap<Symbol, HashMap<Symbol, Type>> scope){
      for (HashMap.Entry<Symbol, HashMap<Symbol, Type>> entry : scope.entrySet()) {
         System.out.println(entry.getKey() + ": " + entry.getValue().toString());
     }
   }
   public static void printScope2(HashMap<Symbol, LinkedList<Symbol>> scope){
      for (HashMap.Entry<Symbol, LinkedList<Symbol>> entry : scope.entrySet()) {
         System.out.println(entry.getKey() + ": " + entry.getValue().toString());
     }
   }
   public static void main(String [] args) {
      try {
         Program root = new MyParser(System.in).Goal();
         //root.accept(new PrettyPrintVisitor());

         SymbolTableVisitor stv = new SymbolTableVisitor();
         root.accept(stv);

         HashMap<Symbol, HashMap<Symbol, Type>> classScope = stv.getClassScope();
         HashMap<Symbol, HashMap<Symbol, HashMap<Symbol, Type>>> methodScope = stv.getMethodScope() ;
         HashMap<Symbol, HashMap<Symbol, LinkedList<Symbol>>> params = stv.getParams();

         TypeCheckVisitor tcv = new TypeCheckVisitor(methodScope, classScope, params);
         root.accept(tcv);

         /*
         System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
         System.out.println("---------- Class Scope: Class = {Attributs:Type && Methods:Type}\n");
         System.out.println(classScope);
         System.out.println("---------- Symbol Table: Class = {Method = {Local:Type && Params:Type}}\n");
         System.out.println(methodScope);
         System.out.println("---------- Symbol Table Params\n");
         System.out.println(params);
         System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
         */

         TranslationVisitor tv = new TranslationVisitor(methodScope, classScope);
         root.accept(tv);

         LinkedList<Frag> fragList = tv.getFrags();
         LinkedList<TraceSchedule> tc = new LinkedList<TraceSchedule>();

         Translation.Tree.Print print = new Translation.Tree.Print(new PrintStream(System.out));
         for(int i=0; i < fragList.size(); ++i){
            System.out.println("name: " + fragList.get(i).frame.name);
            //print.prStm(fragList.get(i).body);
           tc.add(new TraceSchedule(new BasicBlocks(Canon.Canon.linearize(fragList.get(i).body))));
         }  

      }
      catch (ParseException e) {
         System.out.println(e.toString());
      }  
   }
}