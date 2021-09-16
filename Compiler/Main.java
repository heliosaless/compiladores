import Semantic.SyntaxTree.*;
import Semantic.Symbol.*;
import Semantic.Visitors.*;
import Translation.Frame.Access;
import Translation.Temp.CombineMap;
import Translation.Temp.DefaultMap;
import Translation.Temp.Temp;
import Translation.Translate.*;
import Translation.Tree.*;
import Lex.*;
import RegAlloc.Liveness;
import RegAlloc.RegAlloc;

import java. util.Iterator;
import java. util.Map;

import Canon.BasicBlocks;
import Canon.TraceSchedule;
import FlowGraph.AssemFlowGraph;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java. util.HashMap;
import java. util.LinkedList;
import java.util.List;

import Graph.*;


public class Main {

   public static void main(String [] args) {
      try {
         Program root = new MyParser(System.in).Goal();
         //root.accept(new PrettyPrintVisitor()); // Print Program

         SymbolTableVisitor stv = new SymbolTableVisitor();
         root.accept(stv); // Symbol Table

         HashMap<Symbol, HashMap<Symbol, Type>> classScope = stv.getClassScope();
         HashMap<Symbol, HashMap<Symbol, HashMap<Symbol, Type>>> methodScope = stv.getMethodScope() ;
         HashMap<Symbol, HashMap<Symbol, LinkedList<Symbol>>> params = stv.getParams();

         TypeCheckVisitor tcv = new TypeCheckVisitor(methodScope, classScope, params);
         root.accept(tcv); // TypeCheck

      

         TranslationVisitor tv = new TranslationVisitor(methodScope, classScope);
         root.accept(tv); // Intermediary Representation

         LinkedList<Frag> fragList = tv.getFrags();
         LinkedList<TraceSchedule> tc = new LinkedList<TraceSchedule>();

         Translation.Tree.Print print = new Translation.Tree.Print(new PrintStream(System.out));
         

         for(int i=0; i < fragList.size(); ++i){
            // Canonical Intermeary Representation
            tc.add(new TraceSchedule(new BasicBlocks(Canon.Canon.linearize(fragList.get(i).body)))); 
         }  

         File file = new File("execute.s");
         try {
            if (file.exists()) {
               file.delete();
            }
            file.createNewFile();
            FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            
            for(int i=0; i < tc.size(); ++i){
               System.out.println("---------- START Fragment " + fragList.get(i).frame.name); 
               
               // Intruction Selection
               List<Assem.Instr> il = fragList.get(i).frame.codegen(tc.get(i).stms.stmToList()); 
               il.remove(1);
               
               // Print Selected Instructions
               for(Assem.Instr a : il){
                  System.out.println(a.format(new Translation.Temp.CombineMap(new Translation.Temp.SimpleTempMap(fragList.get(i).frame.tempMapGetter()), new DefaultMap())));
               }
               
               RegAlloc regAloc = new RegAlloc(fragList.get(i).frame, il);
               HashMap<Temp, String> definiedColors = regAloc.getColors();
               
               for (Assem.Instr a : il) {
                  bw.write(a.format(
                     new Translation.Temp.CombineMap(
                        new Translation.Temp.SimpleTempMap(fragList.get(i).frame.tempMapGetter()), new Translation.Temp.SimpleTempMap(definiedColors))));
                  bw.write("\n");
               }
            }
            bw.close();
            fw.close();
         }catch(Exception e){}
    
      }
      catch (ParseException e) {
         System.out.println(e.toString());
      }  
   }
}