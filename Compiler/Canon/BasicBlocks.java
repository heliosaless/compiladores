package Canon;

public class BasicBlocks {
  public StmListList blocks;
  public Translation.Temp.Label done;

  private StmListList lastBlock;
  private Translation.Tree.StmList lastStm;

  private void addStm(Translation.Tree.Stm s) {
	lastStm = lastStm.tail = new Translation.Tree.StmList(s,null);
  }

  private void doStms(Translation.Tree.StmList l) {
      if (l==null) 
	doStms(new Translation.Tree.StmList(new Translation.Tree.JUMP(done), null));
      else if (l.head instanceof Translation.Tree.JUMP 
	      || l.head instanceof Translation.Tree.CJUMP) {
	addStm(l.head);
	mkBlocks(l.tail);
      } 
      else if (l.head instanceof Translation.Tree.LABEL)
           doStms(new Translation.Tree.StmList(new Translation.Tree.JUMP(((Translation.Tree.LABEL)l.head).label), 
	  			   l));
      else {
	addStm(l.head);
	doStms(l.tail);
      }
  }

  void mkBlocks(Translation.Tree.StmList l) {
     if (l==null) return;
     else if (l.head instanceof Translation.Tree.LABEL) {
	lastStm = new Translation.Tree.StmList(l.head,null);
        if (lastBlock==null)
  	   lastBlock= blocks= new StmListList(lastStm,null);
        else
  	   lastBlock = lastBlock.tail = new StmListList(lastStm,null);
	doStms(l.tail);
     }
     else mkBlocks(new Translation.Tree.StmList(new Translation.Tree.LABEL(new Translation.Temp.Label()), l));
  }
   

  public BasicBlocks(Translation.Tree.StmList stms) {
    done = new Translation.Temp.Label();
    mkBlocks(stms);
  }
}
