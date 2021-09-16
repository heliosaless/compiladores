package Translation.Temp;
import java. util.HashMap;
public class SimpleTempMap implements TempMap {
	HashMap<Translation.Temp.Temp,String> tmap1;
	public String tempMap(Temp t) {
	   String s = tmap1.get(t);
	   if (s!=null) return s;
       return null;
	}

	public SimpleTempMap(HashMap<Translation.Temp.Temp,String> t1) {
	   tmap1=t1;
	}
}
