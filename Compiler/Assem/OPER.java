package Assem;

public class OPER extends Instr {
   public Translation.Temp.TempList dst;   
   public Translation.Temp.TempList src;
   public Targets jump;

   public OPER(String a, Translation.Temp.TempList d, Translation.Temp.TempList s, Translation.Temp.LabelList j) {
      assem=a; dst=d; src=s; jump=new Targets(j);
   }
   public OPER(String a, Translation.Temp.TempList d, Translation.Temp.TempList s) {
      assem=a; dst=d; src=s; jump=null;
   }

   public Translation.Temp.TempList use() {return src;}
   public Translation.Temp.TempList def() {return dst;}
   public Targets jumps() {return jump;}

}