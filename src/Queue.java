import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

public class Queue 
{

  List<Node> queue;
  int size = 0;

  public Queue()
  {
    System.out.println("Creating queue");
    queue = new ArrayList<Node>();
  }

  public void addElement(Node node)
  {
    queue.add(node);
    size++;
  }

  public Node getSmallerElement()
  {
    Collections.sort(queue, new NodeSorter() );

    //Get and remove the first element of the list 
    Node returnNode;
    try{
      returnNode  =  queue.remove(0);
      
    }
    catch(IndexOutOfBoundsException e)
    {
      System.out.println("There is no more elements in the list");
      return null;
    }

    size--;

    return returnNode;
  }

  public int getSize()
  {
    return this.size;
  }

}
