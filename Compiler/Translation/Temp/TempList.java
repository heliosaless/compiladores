package Translation.Temp;

public class TempList {
   public Temp head;
   public TempList tail;
   public TempList(Temp h, TempList t) {head=h; tail=t;}
   
   public boolean contains(Temp a){
      TempList aux = tail;
      if(head == a) return true; 
      while(aux != null && aux.head!=null){
         if(aux.head == a) return true;
         aux = aux.tail;
      }
      return false;
   }
}
