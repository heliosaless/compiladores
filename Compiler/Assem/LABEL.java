package Assem;

public class LABEL extends Instr {
   public Translation.Temp.Label label;

   public LABEL(String a, Translation.Temp.Label l) {
      assem=a; label=l;
   }

   public Translation.Temp.TempList use() {return null;}
   public Translation.Temp.TempList def() {return null;}
   public Targets jumps()     {return null;}

}