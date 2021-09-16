package RegAlloc;
import Graph.Node;

public class Move {
    public Node src, dst;
    public Move(Node src, Node dst) {this.src=src; this.dst=dst;}
    public String toString(){
        return "{ " + src + " " + dst + " }";
    }

    public int hashCode() {
        return (src.toString() + dst.toString()).hashCode();
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Move other = (Move) obj;
        if (!src.toString().equals(other.src.toString()) || !dst.toString().equals(other.dst.toString()))
            return false;
        return true;

    }
}
