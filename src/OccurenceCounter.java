import java.util.Hashtable;
import java.util.Set;

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
  public Word[] getWordPerOccurences()
  {
    int hashSize = hash.size();
    Word[] outputArray = new Word[hashSize];

    Set<String> keys = hash.keySet();

    int counter = 0;

    for(String key: keys)
    {
      outputArray[counter] = new Word(key, hash.get(key));
      counter++;
    }

    return outputArray;
  }

  
}
