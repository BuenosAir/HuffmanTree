import java.util.Comparator;

public class NodeSorter implements Comparator<Node> {

  public int compare(Node a, Node b) {
    if(a.getWeight() > b.getWeight())
    {
      return 1;
    }
    else if(a.getWeight() == b.getWeight())
    {
      return 0;
    }
    else 
    {
      return -1;
    }
  }
}
