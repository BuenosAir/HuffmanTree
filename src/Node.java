import java.util.Hashtable;

public class Node {

  Node leftChild;
  Node rightChild;

  Word word;
  int weight;

  public Node()
  {
  }

  public Node (Word word)
  {
    this.word = word;
    this.weight = word.getOccurences();
  }

  public Node(Node leftChild, Node rightChild)
  {
    this.leftChild = leftChild;
    this.rightChild = rightChild;

    //Calculate the weight of the node
    this.weight = leftChild.getWeight() + rightChild.getWeight();
  }


  public static Node createHuffmanTree(Word[] words)
  {
    System.out.println("Creating Huffman tree");

    int numberOfWords = words.length;

    Node rootNode = null;

    Queue queue = new Queue();

    //Create all the nodes and add it to the queue
    for(int wordNumber = 0; wordNumber < numberOfWords; wordNumber++)
    {
      queue.addElement(new Node(words[wordNumber]));
    }


    //Construct the tree
    while(queue.getSize() > 1)
    {
      Node firstLeaf = queue.getSmallerElement();

      //Could be null, it's not a problem
      Node secondLeaf = queue.getSmallerElement();


      Node newNode = new Node(firstLeaf, secondLeaf);

      //The newNode is our new root
      rootNode = newNode;

      //We used two elements to build only ony, the queue size decrease of 1 every iteration
      queue.addElement(newNode);
    }


    return rootNode;
  }

  public void printTableCode()
  {
    if(this.leftChild != null)
    {
      this.leftChild.printNodeCode("0");
    }
    if(this.rightChild != null)
    {
      this.rightChild.printNodeCode("1");
    }
  }

  public void printNodeCode(String code)
  {
    if(leftChild == null && rightChild == null)
    {
      System.out.println(code + " - " + this.word.getContent());
    }
    else 
    {
      if(leftChild != null)
      {
        leftChild.printNodeCode(code + "0");
      }
      if(rightChild != null)
      {
        rightChild.printNodeCode(code + "1");
      }
    }
  }

  public Hashtable<String, String>  getEncodedCharacterList()
  {
    Hashtable <String, String> returnList = new Hashtable<String, String>();

    findNextCode(returnList, "");

    return returnList;
  }
  
  public void findNextCode(Hashtable<String, String> table, String code)
  {
    if(this.leftChild == null && this.rightChild == null)
    {
      if(this.word == null)
      {
        //TODO: Thow exception
        System.out.println("There is no linked word ?!" + code);
      }

      table.put(this.word.getContent(), code);
    }

    if(this.leftChild != null)
    {
      this.leftChild.findNextCode(table, code + "0");
    }
    if(this.rightChild != null)
    {
      this.rightChild.findNextCode(table, code + "1");
    }
  }


  public int getWeight()
  {
    return this.weight;
  }

  public Node getLeftChild()
  {
    return this.leftChild;
  }

  public Node getRightChild()
  {
    return this.rightChild;
  }

  public void setLeftChild(Node child)
  {
    this.leftChild = child;
  }

  public void setRightChild(Node child)
  {
    this.rightChild = child;
  }

  public Word getWord()
  {
    return this.word;
  }

  public void setWord(Word tmpWord)
  {
    this.word = tmpWord;
  }

}
