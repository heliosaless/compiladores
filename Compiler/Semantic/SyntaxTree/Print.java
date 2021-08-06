package Semantic.SyntaxTree;
import Semantic.Visitors.*;

import Translation.Translate.TranslateExp;
import Translation.Translate.TypeTranslationVisitor;

public class Print extends Statement {
  public Exp e;

  public Print(Exp ae) {
    e=ae; 
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