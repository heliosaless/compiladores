
package Translation.Tree;


public class NAME extends Exp {
  public Translation.Temp.Label label;
  public NAME(Translation.Temp.Label l) {label=l;}
  public ExpList kids() {return null;}
  public Exp build(ExpList kids) {return this;}
}
