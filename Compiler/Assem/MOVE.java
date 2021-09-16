package Assem;

public class MOVE extends Instr {
   public Translation.Temp.Temp dst;   
   public Translation.Temp.Temp src;

   public MOVE(String a, Translation.Temp.Temp d, Translation.Temp.Temp s) {
      assem=a; dst=d; src=s;
   }
   public Translation.Temp.TempList use() {return new Translation.Temp.TempList(src,null);}
   public Translation.Temp.TempList def() {return new Translation.Temp.TempList(dst,null);}
   public Targets jumps()     {return null;}

}