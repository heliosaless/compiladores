package Translation.Tree;

public class TEMP extends Exp {
  public Translation.Temp.Temp temp;
  public TEMP(Translation.Temp.Temp t) {temp=t;}
  public ExpList kids() {return null;}
  public Exp build(ExpList kids) {return this;}
}
