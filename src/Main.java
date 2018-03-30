

public class Main {

  public static void main(String []args)
  {
    System.out.println("Starting programm");

    System.out.println(args);
    CompressedFile compressedFile = new CompressedFile("./bible.txt");

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
