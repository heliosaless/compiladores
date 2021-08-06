package Semantic.SyntaxTree;
import Semantic.Visitors.*;
import Translation.Translate.TypeTranslationVisitor;
import Translation.Translate.TranslateExp;

public class IntegerType extends Type {
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