import java.util.Arrays;

public class Main {

  public static void main(String []args)
  {
    System.out.println("Salut");

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

    System.out.println(Arrays.toString(counter.getWordPerOccurences()));
  }

}
