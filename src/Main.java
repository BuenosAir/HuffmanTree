import java.util.Arrays;

public class Main {

  public static void main(String []args)
  {
    System.out.println("Salut");

    Word[] testArr = createArrayOfSortedWords();

    System.out.println(Arrays.toString(testArr));

    Node node = Node.createHuffmanTree(testArr);

    node.printTableCode();
  }

  public static  Word[] createArrayOfSortedWords()
  {
    OccurenceCounter counter=  new OccurenceCounter();

    counter.addWordToCounter("salut");
    counter.addWordToCounter("salut");
    counter.addWordToCounter("salut");
    counter.addWordToCounter("salut");
    counter.addWordToCounter("coucou");
    counter.addWordToCounter("coucou");
    counter.addWordToCounter("coucou");
    counter.addWordToCounter("coucou");
    counter.addWordToCounter("coucou");
    counter.addWordToCounter("coucou2");
    counter.addWordToCounter("coucou2");
    counter.addWordToCounter("coucou2");
    counter.addWordToCounter("coucou2");
    counter.addWordToCounter("coucou2");
    counter.addWordToCounter("coucou2");

    return counter.getWordPerOccurences();
  }

}
