package Semantic.SyntaxTree;
import Semantic.Visitors.*;
import Translation.Translate.TypeTranslationVisitor;
import Translation.Translate.TranslateExp;

public class IdentifierType extends Type {
  public String s;

  public IdentifierType(String as) {
    s=as;
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