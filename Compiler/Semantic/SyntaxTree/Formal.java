package Semantic.SyntaxTree;
import Translation.Translate.TypeTranslationVisitor;
import Translation.Translate.TranslateExp;
import Semantic.Visitors.*;
public class Formal {
  public Type t;
  public Identifier i;
 
  public Formal(Type at, Identifier ai) {
    t=at; i=ai;
  }
    public TranslateExp accept(TypeTranslationVisitor v) {
      return v.visit(this);
    }
  
  public void accept(Visitor v) {
    v.visit(this);
  }

  public Type accept(TypeVisitor v) {
    return v.visit(this);
  }
}