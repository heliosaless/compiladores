package RegAlloc;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;

import FlowGraph.AssemFlowGraph;
import FlowGraph.FlowGraph;
import Graph.*;
import Translation.Temp.*;
public class Liveness extends InterferenceGraph{

    public HashMap<Node, Temp> vars; 
    public HashMap<Temp, Node> igNodes; 
    public MoveList mv;

    //   public HashMap<Node, List<Temp>> liveMap;

    public Liveness(FlowGraph fg){
        // liveMap = new HashMap<Node, List<Temp>>();
        vars = new HashMap<>();
        igNodes = new HashMap<>();
        mv = null;

        HashMap<Node, LinkedList<Temp>> in = ((AssemFlowGraph) fg).in;
        HashMap<Node, LinkedList<Temp>> out = ((AssemFlowGraph) fg).out;
        

        NodeList n2 = fg.nodes();
        while(n2 != null && n2.head != null){
            Node n = n2.head;

            // for (int i = 0; i < m.get(n).size(); ++i) {
            //     if( !igNodes.containsKey(in.get(n).get(i)) ){
            //         Node novo = this.newNode();
            //         vars.put(novo, in.get(n).get(i));
            //         igNodes.put(in.get(n).get(i), novo);
            //     }
            // }

            // for (int i = 0; i < out.get(n).size(); ++i) {
            //     if( !igNodes.containsKey(out.get(n).get(i)) ){
            //         Node novo = this.newNode();
            //         vars.put(novo, out.get(n).get(i));
            //         igNodes.put(out.get(n).get(i), novo);
            //     }
            // }
            List<Temp> uses = ((AssemFlowGraph) fg).tempListConvertToListTemp(fg.use(n));
            List<Temp> defs = ((AssemFlowGraph) fg).tempListConvertToListTemp(fg.def(n));

            for (int i = 0; i < uses.size(); ++i) {
                if( !igNodes.containsKey(uses.get(i)) ){
                    Node novo = this.newNode();
                    vars.put(novo, uses.get(i));
                    igNodes.put(uses.get(i), novo);
                }
            }

            for (int i = 0; i < defs.size(); ++i) {
                if (!igNodes.containsKey(defs.get(i))) {
                    Node novo = this.newNode();
                    vars.put(novo, defs.get(i));
                    igNodes.put(defs.get(i), novo);
                }
            }

            n2 = n2.tail;
        }



        NodeList nl = fg.nodes();
        while(nl != null && nl.head != null){
            Node n = nl.head;

            List<Temp> uses = ((AssemFlowGraph) fg).tempListConvertToListTemp(fg.use(n));                
            List<Temp> defs = ((AssemFlowGraph) fg).tempListConvertToListTemp(fg.def(n));
            
            for(int i = 0; i < defs.size(); ++i){
                Temp a = defs.get(i);
                for(int j = 0; j < out.get(n).size(); ++j){
                    Temp t = out.get(n).get(j);
                    if(fg.isMove(n)){
                        if(!uses.contains(t)){
                            this.addEdge(igNodes.get(a), igNodes.get(t));
                            mv = new MoveList(igNodes.get(a), igNodes.get(t), mv);
                        }
                    }else this.addEdge(igNodes.get(a), igNodes.get(t));
                }
            }
            nl = nl.tail;
        }

    }

    @Override
    public Node tnode(Temp temp) {
        return igNodes.get(temp);
    }

    @Override
    public Temp gtemp(Node node) {
        return vars.get(node);
    }

    @Override
    public MoveList moves() {
        return mv;
    }
}
