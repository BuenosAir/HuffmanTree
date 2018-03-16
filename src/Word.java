public class Word 
{

  private int occurences;
  private String content;

  public Word(String content, int occurences)
  {
    this.occurences = occurences;
    this.content = content;
  }

  public Word(String content)
  {
    this.content = content;
  }

  public int getOccurences()
  {
    return this.occurences;
  }

  public static  int compare(Word firstWord, Word secondWord)
  {
    System.out.println(firstWord.getContent() + " " + secondWord.getContent());
    return firstWord.getOccurences() - secondWord.getOccurences();
  }

  public String getContent()
  {
    return this.content;
  }

  public String toString()
  {
    return this.content + " " + this.occurences;
  }
}
