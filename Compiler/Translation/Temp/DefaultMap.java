package Translation.Temp;

public class DefaultMap implements TempMap {
	public String tempMap(Temp t) {
	   return t == null ? null : t.toString();
	}

	public DefaultMap() {}
}