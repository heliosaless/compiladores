package Translation.Tree;
public class ExpList {
  public Exp head;
  public int length = 0;
  public ExpList tail;
  public ExpList(Exp h, ExpList t) {head=h; tail=t; length = t != null ? t.length  + 1 : 0;}
}


