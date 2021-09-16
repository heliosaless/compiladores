package RegAlloc;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import Assem.*;
import FlowGraph.AssemFlowGraph;
import Graph.Node;
import Graph.NodeList;

import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import Translation.Frame.Frame;
import Translation.Mips.InFrame;
import Translation.Temp.*;


public class RegAlloc {
    Frame frame;
    List<Instr> il;
    AssemFlowGraph flowGraph;

    HashMap<Temp, List<Instr>> moveList;
    LinkedList<Temp> precolored;

    List<Temp> initial;
    List<Temp> spilledNodes;

    List<Temp> simplifyWorkList;    
    List<Temp> freezeWorkList;      
    List<Temp> spillWorklist;
    List<Temp> coalescedNodes;

    List<Instr> coalescedMoves;
    List<Instr> constrainedMoves;
    List<Instr> frozenMoves;
    List<Instr> workListMoves;
    List<Instr> activeMoves;
    
    List<Temp> coloredNodes;
    Stack<Temp> selectStack;


    Set<Edge> adjSet;
    HashMap<Temp, List<Temp>> adjList;
    HashMap<Temp, Integer> degree;

    HashMap<Temp, Temp> alias;
    HashMap<Temp, Temp> color;

    HashMap<Temp, String> definitiveColors;

    Temp fp;
    int k;

    public RegAlloc(Frame frame, List<Instr> il){
        this.frame = frame;
        this.il = il;
        
        initial             =   new LinkedList<>();
        precolored          =   new LinkedList<>();
        spilledNodes        =   new LinkedList<>();
        coloredNodes        =   new LinkedList<>();
        selectStack         =   new Stack<>();
        
        simplifyWorkList    =   new LinkedList<>();
        freezeWorkList      =   new LinkedList<>();
        spillWorklist       =   new LinkedList<>();
        coalescedNodes      =   new LinkedList<>();
        
        coalescedMoves      =   new LinkedList<>();
        workListMoves       =   new LinkedList<>();
        activeMoves         =   new LinkedList<>();
        constrainedMoves    =   new LinkedList<>();
        frozenMoves         =   new LinkedList<>();
        moveList            =   new HashMap<>();
        
        alias               =   new HashMap<>();
        color               =   new HashMap<>();

        adjSet              =   new HashSet<>();
        adjList             =   new HashMap<>();
        degree              =   new HashMap<>();

        
        HashMap<Temp, String> tmap = frame.tempMapGetter();
        k = tmap.size();
        
        for (Temp temp : tmap.keySet()) {
            if(tmap.get(temp).equals("$fp")){
                fp = temp;
            }
            /*if(temps.isEmpty())*/ precolored.add(temp);
            color.put(temp,temp);
        }
        
        flowGraph = new AssemFlowGraph(il);
        NodeList nl = flowGraph.nodes();
        while (nl!= null && nl.head!=null) {
            Node u = nl.head;
            List<Temp> uses = flowGraph.tempListConvertToListTemp(flowGraph.use(u));
            List<Temp> defs = flowGraph.tempListConvertToListTemp(flowGraph.def(u));
            
            List<Temp> tempList;
            tempList = uses;
            for(Temp temp : tempList)
                if(!precolored.contains(temp) && !initial.contains(temp)) initial.add(temp);
            tempList = defs;
            for(Temp temp : tempList)
                if(!precolored.contains(temp) && !initial.contains(temp)) initial.add(temp);
            
            nl=nl.tail;
        }

        while(true){
            flowGraph = new AssemFlowGraph(il);
            init();
            build();
            makeWorkList();
            while( !( simplifyWorkList.isEmpty() && freezeWorkList.isEmpty() && spillWorklist.isEmpty() && workListMoves.isEmpty() )){
                
                if(!simplifyWorkList.isEmpty())      simplify();
                else if(!workListMoves.isEmpty())    coalesce();   
                else if(!freezeWorkList.isEmpty())   freeze();    
                else if(!spillWorklist.isEmpty())    selectSpill();  
                else {}

            }
            assignColors();
            if(!spilledNodes.isEmpty()) rewriteProgram();
            else break;
        }

        definitiveColors = new HashMap<Temp, String>();
        for (Temp temp : color.keySet()) {
            Temp cor = color.get(temp);
            definitiveColors.put(temp, tmap.get(cor));
        }


        ListIterator<Instr> it = il.listIterator();
        while(it.hasNext()) {
            Instr instr = it.next();
            if(instr instanceof MOVE){
                MOVE mov = ((MOVE)instr);
                Temp src = mov.src;
                Temp dst = mov.dst;
                if(definitiveColors.get(src).equals(definitiveColors.get(dst))) it.remove();
            }
        }
     

    }


    public HashMap<Temp, String> getColors(){
        return definitiveColors;
    }


    public static List<Temp> union(List<Temp> a, List<Temp> b){
        HashSet<Temp> c = new HashSet<Temp>();
        List<Temp> d = new LinkedList<>();
        for(int i=0; i < a.size(); ++i){
            c.add(a.get(i));
        }
        for(int i=0; i < b.size(); ++i){
            c.add(b.get(i));
        }
        Iterator<Temp> it = c.iterator();
        while(it.hasNext()){
            d.add(it.next());
        }
        return d;

    }

    public static List<Instr> unionInstr(List<Instr> a, List<Instr> b){
        HashSet<Instr> c = new HashSet<Instr>();
        List<Instr> d = new LinkedList<>();
        for(int i=0; i < a.size(); ++i){
            c.add(a.get(i));
        }
        for(int i=0; i < b.size(); ++i){
            c.add(b.get(i));
        }
        Iterator<Instr> it = c.iterator();
        while(it.hasNext()){
            d.add(it.next());
        }
        return d;

    }

    public static List<Temp> intersection(List<Temp> a, List<Temp> b){
        List<Temp> c = new LinkedList<>();
        if(a!=null) c.addAll(a);
        if (b != null)c.retainAll(b);
        return c;
    
    }


    public static List<Temp> difference(List<Temp> a, List<Temp> b){
        List<Temp> c = new LinkedList<>();
        c.addAll(a);
        c.removeAll(b);
        return c;
    }

    public static List<Instr> intersectionInstr(List<Instr> a, List<Instr> b){
        List<Instr> c = new LinkedList<>();
        if(a!=null) c.addAll(a);
        if(b!=null)c.retainAll(b);
        return c;
    
    }


    public static List<Instr> differenceInstr(List<Instr> a, List<Instr> b){
        List<Instr> c = new LinkedList<>();
        c.addAll(a);
        c.removeAll(b);
        return c;
    }

    public void init(){

        adjSet.clear();
        adjList.clear();
        degree.clear();

        NodeList nl = flowGraph.nodes();
        while (nl != null && nl.head != null) {
            Node u = nl.head;
            List<Temp> uses = flowGraph.tempListConvertToListTemp(flowGraph.use(u));
            List<Temp> defs = flowGraph.tempListConvertToListTemp(flowGraph.def(u));

            List<Temp> tempList;
            tempList = uses;
            for (Temp temp : tempList) {
                if (!precolored.contains(temp) && !degree.containsKey(temp))
                    degree.put(temp, 0);
                if (!precolored.contains(temp) && !adjList.containsKey(temp))
                    adjList.put(temp, new LinkedList<>());
            }
            tempList = defs;
            for (Temp temp : tempList) {
                if (!precolored.contains(temp) && !degree.containsKey(temp))
                    degree.put(temp, 0);
                if (!precolored.contains(temp) && !adjList.containsKey(temp))
                    adjList.put(temp, new LinkedList<>());
            }
            nl = nl.tail;
        }
    }

    public void build(){
        HashMap<Node, LinkedList<Temp>> outs = flowGraph.out;
        for(int j= il.size()-1 ; j >= 0; --j){
            Instr i = il.get(j);
            Node node = flowGraph.getNode(i);
            List<Temp> live = outs.get(node);
            List<Temp> defs = flowGraph.tempListConvertToListTemp(flowGraph.def(node));
            List<Temp> uses = flowGraph.tempListConvertToListTemp(flowGraph.use(node));
            

            if(flowGraph.isMove(node)){
                live = difference(live, uses);
                for( Temp n : union(defs, uses)){
                    List<Instr> instrs = moveList.containsKey(n) ?  moveList.get(n) : new LinkedList<>();
                    instrs.add(i);
                    moveList.put(n, instrs);

                }
                workListMoves.add(i);
            }
            live = union(live, defs);
            for(Temp d : defs){
                for(Temp l : live){
                    addEdge(l, d);
                }
            }
            live = union(uses, difference(live, defs));
        }
    }

    public void makeWorkList(){
        while(!initial.isEmpty()){
            Temp n = initial.get(0);
            initial.remove(n);
            if(degree.get(n) >= k){
                spillWorklist.add(n);
            }else if(MoveRelated(n)){
                freezeWorkList.add(n);
            }else{
                simplifyWorkList.add(n);
            }
        }
    }

    public List<Temp> adjacent(Temp n){
        List<Temp> unitedLists = union(selectStack, coalescedNodes);
        return difference(adjList.get(n), unitedLists);
    } 

    public List<Instr> nodeMoves(Temp n){
        return intersectionInstr(moveList.get(n), unionInstr(activeMoves, workListMoves));
    }


    Boolean MoveRelated(Temp n){
        if(!moveList.containsKey(n)) return false;
        return !moveList.get(n).isEmpty();
    }

    public void simplify(){
        Temp n = simplifyWorkList.remove(0);
        selectStack.push(n);
        List<Temp> adjList = adjacent(n);
        for(int i=0; i < adjList.size(); ++i){
            if(!precolored.contains(adjList.get(i)))
                decrementDegree(adjList.get(i));
        }

    }

    public void decrementDegree(Temp m){
        if(!degree.containsKey(m)) return; 
        int d = degree.get(m);
        degree.put(m, d-1);
        if(d == k){
            List<Temp> list = new LinkedList<>();
            list.addAll(adjacent(m));
            list.add(m);
            enableMoves(list);

            spillWorklist.remove(m);
            if(MoveRelated(m)){
                freezeWorkList.add(m);
            }else{
                simplifyWorkList.add(m);
            }

        }

    }

    public void enableMoves(List<Temp> nl){
        for(int i=0; i < nl.size(); ++i){
            List<Instr> nodeMoves = nodeMoves(nl.get(i));
            for(int j=0; j < nodeMoves.size(); ++j){
                Instr m = nodeMoves.get(j);
                if(activeMoves.contains(m)){
                    activeMoves.remove(m);
                    workListMoves.add(m);
                }
            }
        }
    }

    public void addWorkList(Temp u){
        if((!precolored.contains(u) && !MoveRelated(u)) && degree.get(u) < k){
            freezeWorkList.remove(u);
            simplifyWorkList.add(u);
        }
    }

    public boolean ok (Temp t, Temp r){
        return (!precolored.contains(t) && degree.get(t) < k )|| (precolored.contains(t) && adjSet.contains(new Edge(t, r))); 
    }

    public boolean conservative(List<Temp> list){
        int k_ = 0;
        for(int i = 0; i < list.size(); ++i){
            if(!precolored.contains(list.get(i)) && degree.get(list.get(i)) >= k){
                k_ = k_ + 1;
            }
        }
        return k_ < k;
    }


    public boolean coalesceCondition(Temp u, Temp v){
        for(Temp t : adjacent(v)){
            if(!ok(t, u)) return false;
        }
        return true;
    }

    public void coalesce(){
        ListIterator<Instr> it = workListMoves.listIterator();
        for (int i = 0; i < workListMoves.size(); ++i) {
            Assem.MOVE m = ((Assem.MOVE) workListMoves.get(i));
            Temp x = getAlias(m.dst);
            Temp y = getAlias(m.src);
            Temp u, v;
            if (precolored.contains(y)) {
                u = y;
                v = x;
            } else {
                u = x;
                v = y;
            }
            workListMoves.remove(m);
            if (u.equals(v)) {
                coalescedMoves.add(m);
                addWorkList(u);
            } else if (precolored.contains(v) && adjSet.contains(new Edge(u, v))) {
                constrainedMoves.add(m);
                addWorkList(u);
                addWorkList(v);
            } else if ((precolored.contains(u) && coalesceCondition(u, v))
                    || (!precolored.contains(u) && conservative(union(adjacent(u), adjacent(v))))) {
                coalescedMoves.add(m);
                combine(u, v);
                addWorkList(u);
            } else {
                activeMoves.add(m);
            }
        }
        
    }


    
    public void combine(Temp u, Temp v){
        if(freezeWorkList.contains(v)){
            freezeWorkList.remove(v);
        }else{
            spillWorklist.remove(v);
        }
        coalescedNodes.add(v);
        alias.put(v, u);
        List<Instr> mlu = moveList.get(u);
        List<Instr> mlv = moveList.get(v);
        moveList.put(u, unionInstr(mlu, mlv));
        List<Temp> nl = new LinkedList<>();
        nl.add(v);
        enableMoves(nl);
        List<Temp> adjV = adjacent(v);
        for(int i=0; i < adjV.size(); ++i ){
            addEdge(adjV.get(i), u);
            decrementDegree(adjV.get(i));
        }
        
        if(!precolored.contains(u) &&  degree.get(u) >= k && freezeWorkList.contains(u)){
            freezeWorkList.remove(u);
            spillWorklist.add(u);
        }
    }

    public void addEdge(Temp u, Temp v){
        if(!adjSet.contains(new Edge(u, v)) && u != v){
            adjSet.add(new Edge(u, v));
            adjSet.add(new Edge(v, u));
            if(!precolored.contains(u)){
                if(adjList.containsKey(u)) adjList.get(u).add(v);
                else{
                    List<Temp> temps = new LinkedList<>();
                    temps.add(v);
                    adjList.put(u, temps);
                }
               if(degree.containsKey(u)) degree.put(u, degree.get(u)+1);
               else degree.put(u, 1);
               
            }
            if(!precolored.contains(v)){
                if(adjList.containsKey(v)) adjList.get(v).add(u);
                else{
                    List<Temp> temps = new LinkedList<>();
                    temps.add(u);
                    adjList.put(v, temps);
                }
               if(degree.containsKey(v)) degree.put(v, degree.get(v)+1);
               else degree.put(v, 1);
            }

        }
    }

    public Temp getAlias(Temp n){
        if(coalescedNodes.contains(n)){
            return getAlias(alias.get(n));
        }else{
            return n;
        }
    }

    public void freeze(){
        Temp u = freezeWorkList.get(0);
        freezeWorkList.remove(u);
        simplifyWorkList.add(u);
        freezeMoves(u);
    }

    
    public void freezeMoves(Temp u){
        Temp v;
        List<Instr> moves_ = nodeMoves(u);
        for(int i = 0; i < moves_.size(); ++i){
            MOVE m = ((MOVE)moves_.get(i));
            if(getAlias(m.src) == getAlias(u)) v = getAlias(m.dst);
            else v = getAlias(m.src);
            
            activeMoves.remove(m);
            frozenMoves.add(m);
            if(freezeWorkList.contains(v) && nodeMoves(v).isEmpty()){
                freezeWorkList.remove(v);
                simplifyWorkList.add(v);
            }
        }
        

    }
    
    public void selectSpill(){

        int actual = -1;
        Temp m = null;
        for (int i = 0; i < spillWorklist.size(); ++i) {
            if(adjList.get(spillWorklist.get(i)).size() > actual){
                actual = adjList.get(spillWorklist.get(i)).size();
                m = spillWorklist.get(i);
            }
        }
        
        spillWorklist.remove(m);
        simplifyWorkList.add(m);
        freezeMoves(m);
    }

    public void assignColors(){
        while(!selectStack.isEmpty()){
            Temp n = selectStack.pop();
            List<Temp> okColors = new LinkedList<Temp>();
            for(Temp pn : precolored){
                okColors.add(pn);
            }
            List<Temp> adjN = adjList.get(n);
            for(int i=0; i < adjN.size(); ++i){
                Temp w = adjN.get(i);
                if( union(coloredNodes, precolored).contains(getAlias(w))){
                    okColors.remove(color.get(getAlias(w)));
                }
            }
            if(okColors.isEmpty()){
                spilledNodes.add(n);
            }else{
                coloredNodes.add(n);
                Temp c = okColors.get(0);
                color.put(n, c);
            }

        }
        for(int i=0; i < coalescedNodes.size(); ++i){
            color.put(coalescedNodes.get(i), color.get(getAlias(coalescedNodes.get(i))));
        }
    }

    public void rewriteProgram(){
        List<Temp> newTemps = new LinkedList<>();
        System.out.println("REWRITE..." + spilledNodes);
        for(int i = 0; i < spilledNodes.size(); ++i){
            Temp spillTemp = spilledNodes.get(i);
            InFrame a = (InFrame) frame.allocLocal(true);
            ListIterator<Instr> it = il.listIterator();
            while(it.hasNext()) {
                Instr instr = it.next(); 
                
                if(instr.def()!= null && instr.def().contains(spillTemp)){
                    Temp newTemp = new Temp();
                    newTemps.add(newTemp);
                    it.add(new Assem.MOVE("move $`d0, $`s0\n", newTemp, spillTemp));
                    it.add(new Assem.OPER("sw $`s0, " + a.offset + "($`s1) \n" , null, new TempList(newTemp, new TempList(fp, null))));
                }
                
                if(instr.use() != null && instr.use().contains(spillTemp)){

                    Temp newTemp = new Temp();
                    newTemps.add(newTemp);
                    if(it.hasPrevious()) it.previous();

                    it.add(new Assem.OPER("lw $`d0, "+ a.offset + "($`s0)\n", new TempList(newTemp, null), new TempList(fp, null)));
                    it.add(new Assem.MOVE("move $`d0, $`s0\n", spillTemp, newTemp));
                    it.next();

                }

            }


        }

        spilledNodes.clear();
        initial = union(coloredNodes, union(coalescedNodes, newTemps));
        coloredNodes.clear();
        coalescedNodes.clear();

        // flowGraph = new AssemFlowGraph(il);
        // flowGraph.show(System.out);


        // for (Instr a : il) {
        //     System.out.println(a.format(new DefaultMap()));
        // }

        // System.exit(0);
    }
    

}


