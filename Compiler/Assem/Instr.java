package Assem;
import Translation.Temp.*;

public abstract class Instr {
  public String assem;
  public abstract Translation.Temp.TempList use();
  public abstract Translation.Temp.TempList def();
  public abstract Targets jumps();

  private Translation.Temp.Temp nthTemp(Translation.Temp.TempList l, int i) {
    if (i==0) return l.head;
    else return nthTemp(l.tail,i-1);
  }

  private Translation.Temp.Label nthLabel(Translation.Temp.LabelList l, int i) {
    if (i==0) return l.head;
    else return nthLabel(l.tail,i-1);
  }

  public String format(Translation.Temp.TempMap m) {
    Translation.Temp.TempList dst = def();
    Translation.Temp.TempList src = use();
    Targets j = jumps();
    Translation.Temp.LabelList jump = (j==null)?null:j.labels;
    StringBuffer s = new StringBuffer();
    int len = assem.length();
    for(int i=0; i<len; i++)
	if (assem.charAt(i)=='`')
	   switch(assem.charAt(++i)) {
              case 's': {int n = Character.digit(assem.charAt(++i),10);
			 s.append(m.tempMap(nthTemp(src,n)));
			}
			break;
	      case 'd': {int n = Character.digit(assem.charAt(++i),10);
			 s.append(m.tempMap(nthTemp(dst,n)));
			}
 			break;
	      case 'j': {int n = Character.digit(assem.charAt(++i),10);
			 s.append(nthLabel(jump,n).toString());
			}
 			break;
	      case '`': s.append('`'); 
			break;
              default: throw new Error("bad Assem format");
       }
       else s.append(assem.charAt(i));

    return s.toString();
  }

    // public int hashCode() {
    //   return this.format(new DefaultMap()).hashCode();
    // }

    // public boolean equals(Object obj) {
    //   if (this == obj)
    //     return true;
    //   if (obj == null)
    //     return false;
    //   if (getClass() != obj.getClass())
    //     return false;
    //   Instr other = (Instr) obj;
    //   if (this.hashCode() != other.hashCode())
    //     return false;
    //   return true;

    // }


}