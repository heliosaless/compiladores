package Semantic.SyntaxTree;
import Semantic.Visitors.*;

import Translation.Translate.TranslateExp;
import Translation.Translate.TypeTranslationVisitor;

public class True extends Exp {
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