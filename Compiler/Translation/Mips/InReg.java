package Translation.Mips;


public class InReg extends Translation.Frame.Access {
    Translation.Temp.Temp temp;
    InReg(Translation.Temp.Temp t) {
	temp = t;
    }

    public Translation.Tree.Exp exp(Translation.Tree.Exp fp) {
        return new Translation.Tree.TEMP(temp);
    }

    public String toString() {
        return temp.toString();
    }
}