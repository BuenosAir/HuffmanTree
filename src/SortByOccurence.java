import java.util.Comparator;

public class SortByOccurence implements Comparator<Word> {

  public int compare(Word a, Word b) {
    if(a.getOccurences() < b.getOccurences())
    {
      return 1;
    }
    else if(a.getOccurences() == b.getOccurences())
    {
      return 0;
    }
    else 
    {
      return -1;
    }
  }
}
