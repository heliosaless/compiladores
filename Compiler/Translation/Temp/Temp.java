package Translation.Temp;

public class Temp  {
   private static int count;
   private int num;
   public String toString() {return "t" + num;}
   public Temp() { 
     num=count++;
   }
//    public int hashCode() {
//     return num;
// }

// public boolean equals(Object obj) {
//     if (this == obj)
//         return true;
//     if (obj == null)
//         return false;
//     if (getClass() != obj.getClass())
//         return false;
//     Temp other = (Temp) obj;
//     if (num != other.num)
//         return false;
//     return true;

// }
}
