package Semantic.SyntaxTree;
import Semantic.Visitors.*;


import Translation.Translate.TranslateExp;
import Translation.Translate.TypeTranslationVisitor;

public abstract class Statement {
  public abstract void accept(Visitor v);
  public abstract Type accept(TypeVisitor v);
  public abstract TranslateExp accept(TypeTranslationVisitor v);
}