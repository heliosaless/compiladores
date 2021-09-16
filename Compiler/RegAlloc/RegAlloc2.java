/*package RegAlloc;
import java.util.List;
import java.util.Stack;

import Assem.Instr;
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


public class RegAlloc2 {
    Frame frame;
    List<Instr> il;
    Liveness interferenceGraph;

    HashMap<Node, List<Node>> moveList;
    LinkedList<Node> precolored;

    List<Node> initial;
    List<Node> spilledNodes;

    List<Node> simplifyWorkList;
    List<Node> freezeWorkList;
    List<Node> spillWorklist;
    List<Node> coalescedNodes;

    List<Move> coalescedMoves;
    List<Move> constrainedMoves;
    List<Move> frozenMoves;
    List<Move> workListMoves;
    List<Move> activeMoves;
    
    List<Node> coloredNodes;
    Stack<Node> selectStack;

    Set<Edge> adjSet;
    HashMap<Node, List<Node>> adjList;
    HashMap<Node, Integer> degree;

    HashMap<Node, Node> alias;
    HashMap<Node, Temp> color;

    HashMap<Temp, String> definitiveColors;

    Temp fp;
    int k;

    public RegAlloc2(Frame frame, List<Instr> il){
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
        
        AssemFlowGraph flowGraph = new AssemFlowGraph(this.il);
        interferenceGraph = new Liveness(flowGraph);
        
        HashMap<Temp, String> tmap = frame.tempMapGetter();
        List<Temp> temps = new LinkedList<>();
        k = tmap.size();
        
        for (Temp temp : tmap.keySet()) {
            if(tmap.get(temp).equals("$fp")){
                fp = temp;
            }
            /*if(temps.isEmpty()) temps.add(temp);
        }
        
        List<Node> a = nodeListConvertToListNode(interferenceGraph.nodes());
        for(int i = 0; i < a.size(); ++i){
            Node u = a.get(i);
            if(temps.contains(interferenceGraph.gtemp(u))){
                precolored.add(u);
                color.put(u, interferenceGraph.gtemp(u));
            } 
            else {
                initial.add(u);
                adjList.put(u, new LinkedList<>());
                degree.put(u, 0);
            }
        }
        
        for(int i = 0; i < a.size(); ++i){
            Node u = a.get(i);
            NodeList nl = u.adj();
            while(nl != null && nl.head !=null){
                Node v = nl.head;
                addEdge(u, v);
                nl = nl.tail;
            }
        }

        System.out.println(degree);

        
        MoveList m = interferenceGraph.moves();
        while(m != null && m.src != null && m.dst != null){
            Move m1 = new Move(m.src, m.dst);
            workListMoves.add(m1);
            List<Node> l1 = new LinkedList<>();
            
            if(moveList.containsKey(m.src)){
                if(!moveList.get(m.src).contains(m.dst)) moveList.get(m.src).add(m.dst);
            }else{
                l1.add(m.dst);
                moveList.put(m.src, l1);
            }
            
            l1 = new LinkedList<>();
            if(moveList.containsKey(m.dst)){
                if (!moveList.get(m.dst).contains(m.src)) moveList.get(m.dst).add(m.src);
            }else{
                l1.add(m.src);
                moveList.put(m.dst, l1);
            }
            
            m = m.tail;
        }
        


        makeWorkList();

        while(true){
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

        System.out.println(color);
        definitiveColors = new HashMap<Temp, String>();
        for (Node node : color.keySet()) {
            Temp temp = interferenceGraph.gtemp(node);
            Temp cor = color.get(node);
            System.out.println(temp + " " +  cor);

            definitiveColors.put(temp, cor.toString());
        }

    }


    public HashMap<Temp, String> getColors(){
        return definitiveColors;
    }

    public static List<Node> union(List<Node> a, List<Node> b){
        HashSet<Node> c = new HashSet<Node>();
        List<Node> d = new LinkedList<>();
        for(int i=0; i < a.size(); ++i){
            c.add(a.get(i));
        }
        for(int i=0; i < b.size(); ++i){
            c.add(b.get(i));
        }
        Iterator<Node> it = c.iterator();
        while(it.hasNext()){
            d.add(it.next());
        }
        return d;

    }

    public static List<Move> unionMove(List<Move> a, List<Move> b){
        HashSet<Move> c = new HashSet<Move>();
        List<Move> d = new LinkedList<>();
        for(int i=0; i < a.size(); ++i){
            c.add(a.get(i));
        }
        for(int i=0; i < b.size(); ++i){
            c.add(b.get(i));
        }
        Iterator<Move> it = c.iterator();
        while(it.hasNext()){
            d.add(it.next());
        }
        return d;

    }

    public static List<Node> intersection(List<Node> a, List<Node> b){
        List<Node> c = new LinkedList<>();
        c.addAll(a);
        c.retainAll(b);
        return c;
    
    }


    public static List<Node> difference(List<Node> a, List<Node> b){
        List<Node> c = new LinkedList<>();
        c.addAll(a);
        c.removeAll(b);
        return c;
    }

    public static List<Move> intersectionMove(List<Move> a, List<Move> b){
        List<Move> c = new LinkedList<>();
        c.addAll(a);
        c.retainAll(b);
        return c;
    
    }


    public static List<Move> differenceMove(List<Move> a, List<Move> b){
        List<Move> c = new LinkedList<>();
        c.addAll(a);
        c.removeAll(b);
        return c;
    }




    public void makeWorkList(){
        for(int i=0; i < initial.size(); ++i){
            // Node n = interferenceGraph.tnode(initial.remove(i));
            Node n = initial.remove(i);
            if(degree.get(n) >= k){
                spillWorklist.add(n);
            }else if(MoveRelated(n)){
                freezeWorkList.add(n);
            }else{
                simplifyWorkList.add(n);
            }
        }
    }

    public List<Node> adjacent(Node n){
        List<Node> unitedLists = union(selectStack, coalescedNodes);
        return difference(adjList.get(n), unitedLists);
    } 

    public List<Move> nodeMoves(Node n){
        List<Move> nl = new LinkedList<>();
        MoveList m = interferenceGraph.moves();
        while(m != null && m.src != null && m.dst != null){
            Move m1 = new Move(m.src, m.dst);
            nl.add(m1);
            // if(n.toString() == m.src.toString()){
            //     nl.add(m.src);
            // }
            // if(n.toString() == m.dst.toString()){
            //     nl.add(m.dst);
            // }
            m = m.tail;
        }
        return intersectionMove(nl, unionMove(activeMoves, workListMoves));
    }


    Boolean MoveRelated(Node n){
        List<Node> nl = new LinkedList<>();
        MoveList m = interferenceGraph.moves();
        while(m != null && m.src != null && m.dst != null){
            if(n.toString() == m.src.toString()){
                nl.add(m.src);
            }
            if(n.toString() == m.dst.toString()){
                nl.add(m.dst);
            }
            m = m.tail;
        }
        return !nl.isEmpty();
    }

    public void simplify(){
        Node n = simplifyWorkList.remove(0);
        selectStack.add(n);
        List<Node> adjList = adjacent(n);
        for(int i=0; i < adjList.size(); ++i){
            if(!precolored.contains(adjList.get(i)))
                decrementDegree(adjList.get(i));
        }

    }

    public void decrementDegree(Node m){
        

        int d = degree.get(m);
        degree.put(m, d-1);
        if(d == k){
            List<Node> list = new LinkedList<>();
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

    public void enableMoves(List<Node> nl){
        for(int i=0; i < nl.size(); ++i){
            List<Move> nodeMoves = nodeMoves(nl.get(i));
            for(int j=0; j < nodeMoves.size(); ++j){
                Move m = nodeMoves.get(j);
                if(activeMoves.contains(m)){
                    activeMoves.remove(m);
                    workListMoves.add(m);
                }
            }
        }
    }

    public void addWorkList(Node u){
        if((!precolored.contains(u) && !MoveRelated(u)) && degree.get(u) < k){
            freezeWorkList.remove(u);
            simplifyWorkList.add(u);
        }
    }

    public boolean ok (Node t, Node r){
        return (!precolored.contains(t) && degree.get(t) < k )|| (precolored.contains(t) && adjSet.contains(new Edge(t, r))); 
    }

    public boolean conservative(List<Node> list){
        int k_ = 0;
        for(int i = 0; i < list.size(); ++i){
            if(degree.get(list.get(i)) >= k){
                k_ = k_ + 1;
            }
        }
        return k_ < k;
    }


    public boolean coalesceCondition(Node u, Node v){
        for(Node t : adjacent(v)){
            if(!ok(t, u)) return false;
        }
        return true;
    }

    public void coalesce(){
        for(int i = 0; i < workListMoves.size(); ++i){
            Move m = workListMoves.get(i);
            Node x = getAlias(m.src);
            Node y = getAlias(m.dst);
            Node u, v;
            if(precolored.contains(y)){
                u = y;
                v = x;
            }else{
                u = x;
                v = y;
            }
            workListMoves.remove(m);
            if(u == v){
                coalescedMoves.add(m);
                addWorkList(u);
            }else if(precolored.contains(v) && adjSet.contains(new Edge(u, v))){
                constrainedMoves.add(m);
                addWorkList(u);
                addWorkList(v);
            }else if((precolored.contains(u) && coalesceCondition(u, v)) || 
                (!precolored.contains(u) && conservative(union(adjacent(u), adjacent(v))))){
                    coalescedMoves.add(m);
                    combine(u, v);
                    addWorkList(u);
            }else{
                activeMoves.add(m);
            }
        }        
    }


    
    public void combine(Node u, Node v){
        System.out.println("NOH AGLUTINADO");
        MoveList m = interferenceGraph.moves();
        if(freezeWorkList.contains(v)){
            freezeWorkList.remove(v);
        }else{
            spillWorklist.remove(v);
        }
        coalescedNodes.add(v);
        alias.put(v, u);
        List<Node> mlu = moveList.get(u);
        List<Node> mlv = moveList.get(v);
        moveList.put(u, union(mlu, mlv));
        List<Node> nl = new LinkedList<>();
        nl.add(v);
        enableMoves(nl);
        List<Node> adjV = adjacent(v);
        for(int i=0; i < adjV.size(); ++i ){
            // interferenceGraph.addEdge(adjV.get(i), u);

            addEdge(adjV.get(i), u);
            decrementDegree(adjV.get(i));
        }
        if(!precolored.contains(u) &&  degree.get(u) >= k && freezeWorkList.contains(u)){
            freezeWorkList.remove(u);
            spillWorklist.add(u);
        }
        

    }

    public void addEdge(Node u, Node v){
        if(!adjSet.contains(new Edge(u, v)) && u != v){
            adjSet.add(new Edge(u, v));
            adjSet.add(new Edge(v, u));
            if(!precolored.contains(u)){
                adjList.get(u).add(v);
                degree.put(u, degree.get(u)+1);
            }
            if(!precolored.contains(v)){
                adjList.get(v).add(u);
                degree.put(v, degree.get(v)+1);
            }

        }
    }

    public Node getAlias(Node n){
        if(coalescedNodes.contains(n)){
            return getAlias(alias.get(n));
        }else{
            return n;
        }
    }

    public void freeze(){
        System.out.println("MOVE CONGELADO");
        for(int i =0; i< freezeWorkList.size(); ++i){
            Node u = freezeWorkList.get(i);
            freezeWorkList.remove(u);
            simplifyWorkList.add(u);
            freezeMoves(u);
        }
    }

    
    public void freezeMoves(Node u){
        Node v;
        for(Move m : nodeMoves(u)){
            if(getAlias(m.dst) == getAlias(u)) v = getAlias(m.src);
            else v = getAlias(m.dst);
            
            activeMoves.remove(m);
            frozenMoves.add(m);
            if(freezeWorkList.contains(v) && nodeMoves(v).isEmpty()){
                freezeWorkList.remove(v);
                simplifyWorkList.add(v);
            }
        }
        

    }
    
    public void selectSpill(){
        System.out.println("SPILL SELECIONADO");
        if(spillWorklist.isEmpty()) return;
        Node m = spillWorklist.get(0); // HEURISTIC TO IMPLEMENT
        spillWorklist.remove(m);
        simplifyWorkList.add(m);
        freezeMoves(m);
    }

    public void assignColors(){
        while(!selectStack.isEmpty()){
            Node n = selectStack.pop();
            List<Temp> okColors = new LinkedList<Temp>();
            for(Node pn : precolored){
                okColors.add(interferenceGraph.gtemp(pn));
            }
            List<Node> adjN = adjList.get(n);
            for(int i=0; i < adjN.size(); ++i){
                Node w = adjN.get(i);
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
        // List<Node> newTemps = new LinkedList<>();
        System.out.println("REWRITE...");
        for(int i = 0; i < spilledNodes.size(); ++i){
            Node node = spilledNodes.get(i);
            Temp v = interferenceGraph.gtemp(node);
            InFrame a = (InFrame) frame.allocLocal(true);
            Temp vi = new Temp();
            for (int j = 0; j < il.size(); ++j) {
                Instr instr = il.get(j); 
                
                if(instr.def().contains(v)){ 
                    il.add(j-1, new Assem.MOVE("move ", vi, v));
                    il.add(j, new Assem.OPER("sw $`s0, " + a.offset + "($`s1) \n" , null, new TempList(vi, new TempList(fp, null))));
                }
                
                if(instr.use().contains(v)){
                    il.add(j, new Assem.OPER("lw $`d0, "+ a.offset + "($`s0)\n", new TempList(vi, null), new TempList(fp, null)));
                    il.add(j+1, new Assem.MOVE("move ", v, vi));
                }

            }


        }


        spilledNodes.clear();
        // initial = union(coloredNodes, union(coalescedNodes, newTemps));
        initial = union(coloredNodes, coalescedNodes);
        coloredNodes.clear();
        coalescedNodes.clear();
    }
    


    public List<Node> nodeListConvertToListNode(NodeList list){
        List<Node> finalList = new LinkedList<Node>();
        NodeList aux = list;
        while(true){
            if(aux == null) break;
            finalList.add(aux.head);
            if(aux.tail == null) break;
            aux = aux.tail;
        }
        return finalList;
    }
}


*/