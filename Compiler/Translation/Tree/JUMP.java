package Translation.Tree;



public class JUMP extends Stm {
  public Exp exp;
  public Translation.Temp.LabelList targets;
  public JUMP(Exp e, Translation.Temp.LabelList t) {exp=e; targets=t;}
  public JUMP(Translation.Temp.Label target) {
      this(new NAME(target), new Translation.Temp.LabelList(target,null));
  }
  public ExpList kids() {return new ExpList(exp,null);}
  public Stm build(ExpList kids) {
    return new JUMP(kids.head,targets);
  }
}
