package Translation.Frame;

public abstract class Access {
  public abstract String toString();
  public abstract Translation.Tree.Exp exp(Translation.Tree.Exp e);
}