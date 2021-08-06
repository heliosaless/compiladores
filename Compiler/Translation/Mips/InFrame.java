package Translation.Mips;

public class InFrame extends Translation.Frame.Access {
    int offset;
    InFrame(int o) {
	offset = o;
    }

    public Translation.Tree.Exp exp(Translation.Tree.Exp fp) {
        return new Translation.Tree.MEM
	    (new Translation.Tree.BINOP(Translation.Tree.BINOP.PLUS, fp, new Translation.Tree.CONST(offset)));
    }

    public String toString() {
        Integer offset = new Integer(this.offset);
	return offset.toString();
    }
}