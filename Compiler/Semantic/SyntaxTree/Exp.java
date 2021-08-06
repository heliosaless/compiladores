package Semantic.SyntaxTree;
import Semantic.Visitors.*;
import Translation.Translate.TypeTranslationVisitor;
import Translation.Translate.TranslateExp;

public abstract class Exp {
  public abstract void accept(Visitor v);
  public abstract Type accept(TypeVisitor v);
  public abstract TranslateExp accept(TypeTranslationVisitor v);
}