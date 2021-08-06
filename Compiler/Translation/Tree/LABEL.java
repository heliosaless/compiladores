package Translation.Tree;


public class LABEL extends Stm { 
  public Translation.Temp.Label label;
  public LABEL(Translation.Temp.Label l) {label=l;}
  public ExpList kids() {return null;}
  public Stm build(ExpList kids) {
    return this;
  }
}
