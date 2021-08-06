package Translation.Translate;


public class TranslateExp {
  private Translation.Tree.Exp exp;

  public TranslateExp(Translation.Tree.Exp e) {
    exp = e;
  }

  public Translation.Tree.Exp unEx() {
    return exp;
  }
}
