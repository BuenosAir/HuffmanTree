import java.util.Hashtable;
import java.util.Set;
import java.util.Arrays;

public class OccurenceCounter 
{
  private Hashtable<String, Integer> hash;

  public OccurenceCounter ()
  {
    System.out.println("Creating an OccurenceCounter");
    hash = new Hashtable<String, Integer>();
  }

  public void addWordToCounter(String word)
  {
    //Check if the work already exist in the hashtable
    int occurences;
    if(hash.containsKey(word))
    {
      occurences = hash.get(word) + 1;
    }
    else 
    {
      occurences = 1;
    }

    //Add/replace the value in the hash
    hash.put(word, occurences);
  }

  //Return an array of all the keys sorted by occurences
  public String[] getWordPerOccurences()
  {
    int hashSize = hash.size();
    Word[] arrayToSort = new Word[hashSize];

    Set<String> keys = hash.keySet();

    int counter = 0;

    for(String key: keys)
    {
      System.out.println(key + " " +  hash.get(key));
      arrayToSort[counter] = new Word(key, hash.get(key));
      counter++;
    }

    System.out.println("Sorting all occurences");
    Arrays.sort(arrayToSort, new SortByOccurence());

    //Keep only the strings, numbers of occurences is not important
    String[] sortedArray = new String[hashSize];
    for(int i = 0; i < hashSize; i++)
    {
      sortedArray[i] = arrayToSort[i].getContent();
    }

    return sortedArray;
  }

  
}
