package FlowGraph;

import Graph.*;
import Assem.*;
import Translation.Temp.*;
import java.util.List;
import java.util.LinkedList;
import java.util.HashMap;

public class AssemFlowGraph extends FlowGraph{
    public HashMap<Node, Instr> myTable;
    public HashMap<Instr, Node> myNodes;
    public HashMap<Node, LinkedList<Temp>> in;
    public HashMap<Node, LinkedList<Temp>> out;
    public HashMap<Node, Boolean> myMoves; 

    public AssemFlowGraph(List<Instr> instrs){
        myTable = new HashMap<Node, Instr>();
        myNodes = new HashMap<Instr, Node>();
        myMoves = new HashMap<>();
        for(int i = 0; i < instrs.size(); i++){
            Node u = this.newNode();
            myTable.put(u, instrs.get(i));
            myNodes.put(instrs.get(i), u);
            
            if( instrs.get(i) instanceof MOVE ){
                myMoves.put(u, true);
            } else{
                myMoves.put(u, false);
            }
        }
        
        for (int i = 0; i < instrs.size(); i++) {
            Node u = myNodes.get(instrs.get(i));
            
            LabelList labelList = null;
            Targets targets = instrs.get(i).jumps();
            if(targets != null)  labelList = targets.labels;
            
            
            NodeList itNodes = this.nodes();
            while(itNodes != null && itNodes.head != null){
                Instr instr = myTable.get(itNodes.head);
                
                if( instr instanceof LABEL ){
                    LabelList it = labelList;
                    while(it != null && it.head != null){
                        
                        if( it.head.toString().equals(((LABEL)instr).label.toString()) ){
                            addEdge(u, itNodes.head);
                        }
                        
                        it = it.tail;
                    }
                }
                
                itNodes = itNodes.tail;
            } 
            
        }
        
        for (int i = 0; i < instrs.size(); i = i + 1) {
            Node u = myNodes.get(instrs.get(i));
            
            if (i + 1 < instrs.size()) {
                Node v = myNodes.get(instrs.get(i+1));
                addEdge(u, v);
            }
        }
        
        HashMap<Node, LinkedList<Temp>> inPrime;
        HashMap<Node, LinkedList<Temp>> outPrime;
        inPrime = new HashMap<>();
        outPrime = new HashMap<>();
        in = new HashMap<>();
        out = new HashMap<>();
        
        for (int i = 0; i < instrs.size(); ++i) {
            Node u = myNodes.get(instrs.get(i));
            
            LinkedList<Temp> inList = new LinkedList<Temp>();
            LinkedList<Temp> outList = new LinkedList<Temp>();
            LinkedList<Temp> inListPrime = new LinkedList<Temp>();
            LinkedList<Temp> outListPrime = new LinkedList<Temp>();
            
            //inList.addAll(tempListConvertToListTemp(this.use(u)));
            //outList.addAll(tempListConvertToListTemp(this.def(u)));
            
            in.put(u, inList);
            out.put(u, outList);
            inPrime.put(u, inListPrime);
            outPrime.put(u, outListPrime);
        }
        
        while (true) {
            for (int i = 0; i < instrs.size(); ++i) {
                Node u = myNodes.get(instrs.get(i));
                
                inPrime.get(u).clear();
                inPrime.get(u).addAll(in.get(u));
                outPrime.get(u).clear();
                outPrime.get(u).addAll(out.get(u));

                LinkedList<Temp> aux = new LinkedList<>();
                for (Temp temp :  out.get(u)) {
                    if(!tempListConvertToListTemp(this.def(u)).contains(temp)) aux.add(temp);
                }
                
                // out.get(u).removeAll(tempListConvertToListTemp(this.def(u)));
                in.get(u).clear();
                in.get(u).addAll(tempListConvertToListTemp(this.use(u)));

                for (Temp t :aux) {
                    if(!in.get(u).contains(t)) in.get(u).add(t);
                }
                out.get(u).clear();
                
                NodeList itNodes = u.succ();
                while(itNodes != null && itNodes.head != null){
                    Node v = itNodes.head;   
                    
                    for (Temp t : in.get(v)) {
                        if (!out.get(u).contains(t))
                            out.get(u).add(t);
                    }
                    itNodes = itNodes.tail;
                }
            }
            
            if(condition(instrs, inPrime, outPrime)) break;
        }
        
    }

    public Node getNode(Instr i){
        return myNodes.get(i);
    } 

    public Instr instr(Node n){
        return myTable.get(n);
    }

    public Translation.Temp.TempList def(Node node){
        return myTable.get(node).def();
    }

    public Translation.Temp.TempList use(Node node){
        return myTable.get(node).use();
    }

    public boolean isMove(Node node){
        return myMoves.get(node);
    }

    public boolean condition (List<Instr> instrs, HashMap<Node, LinkedList<Temp>> inPrime, HashMap<Node, LinkedList<Temp>> outPrime) {

        for (int i = 0; i < instrs.size(); ++i) {
            Node u = myNodes.get(instrs.get(i));

            if (!(inPrime.get(u).equals(in.get(u)) && outPrime.get(u).equals(out.get(u)))) return false;
  
        }
        return true;
    }

    public List<Temp> tempListConvertToListTemp (TempList tl) {
        List<Temp> finalList = new LinkedList<Temp> ();

        while(true){
            if(tl == null) break;
            finalList.add(tl.head);
            if(tl.tail == null) break;
            tl = tl.tail;
        }
        return finalList;
    }
        
}
