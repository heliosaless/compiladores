package Translation.Tree;

import java.util.LinkedList;
import java.util.List;

public class StmList {
  public Stm head;
  public StmList tail;
  public StmList(Stm h, StmList t) {head=h; tail=t;}

  public List<Stm> stmToList() {
     List<Stm> sli = new LinkedList<>();
     StmList aux = this;

     while (true) {
      sli.add(aux.head);
      if(aux.tail == null) break;
      aux = aux.tail;
     } 
     return sli;
  }
}


