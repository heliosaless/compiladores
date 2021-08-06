package Semantic.SyntaxTree;
import Semantic.Visitors.*;
import Translation.Translate.TypeTranslationVisitor;
import Translation.Translate.TranslateExp;

public class Call extends Exp {
  public Exp e;
  public Identifier i;
  public ExpList el;
  
  public Call(Exp ae, Identifier ai, ExpList ael) {
    e=ae; i=ai; el=ael;
  }

  public void accept(Visitor v) {
    v.visit(this);
  }

  public TranslateExp accept(TypeTranslationVisitor v) {
    return v.visit(this);
  }

  public Type accept(TypeVisitor v) {
    return v.visit(this);
  }
}