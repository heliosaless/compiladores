package RegAlloc;
import Translation.Temp.Temp;


public class Edge {
    public Temp src, dst;
    public Edge(Temp src, Temp dst) {this.src=src; this.dst=dst;}    
    public int hashCode(){
        return (src.toString() + dst.toString()).hashCode();
    }
    public boolean equals(Object obj){
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Edge other = (Edge) obj;
        if ( !src.toString().equals(other.src.toString()) || !dst.toString().equals(other.dst.toString()))
            return false;
        return true;

    }
}
