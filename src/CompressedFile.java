import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.Hashtable;
import java.util.Set;

@SuppressWarnings("unchecked") //We must suppress this warning caused by the import of the Hashtable storing the serialized codeTable, will be corrected by hashing the input file

public class CompressedFile
{

  public static final int OUTPUT_BUFFER_SIZE = 4 * 5000;

  public static final int READING_BUFFER_SIZE = 4 * 5000;


  public CompressedFile(String path)
  {
    System.out.println("Opening " + path);

    BufferedReader bufferReader = null;
    FileReader fileReader = null;

    OccurenceCounter occurenceCounter = new OccurenceCounter();

    char[] readingBuffer = new char[READING_BUFFER_SIZE];

    try {

      //Get the file size for the progress bar
      File file = new File(path);
      long fileSize = file.length();

      System.out.println("Filesize : " + fileSize);

      fileReader = new FileReader(path);
      bufferReader = new BufferedReader(fileReader);

      long totalBytesReaden = 0;

      System.out.println("Analysing file ...");

      long timestamp = System.currentTimeMillis();
      long readDuringTwoTimestamp = 0;

      while(bufferReader.read(readingBuffer) != -1 )
      {

        for(int i = 0; i < READING_BUFFER_SIZE; i++)
        {
          occurenceCounter.addWordToCounter(String.valueOf(readingBuffer[i]));
        }

        totalBytesReaden += READING_BUFFER_SIZE;
        readDuringTwoTimestamp +=READING_BUFFER_SIZE;

        if(System.currentTimeMillis() - timestamp > 1000)
        {
          System.out.println(
              "Read speed : " + (readDuringTwoTimestamp / Math.pow(1024, 2))
              + " Mo/s, progress : " + (100 * totalBytesReaden / fileSize) +"%"
              );
          timestamp = System.currentTimeMillis();
          readDuringTwoTimestamp = 0;
        }

      }

    } catch (IOException e) {

      e.printStackTrace();

    } finally {

      try {
        if (bufferReader != null)
        {
          bufferReader.close();
        }
        if (fileReader!= null)
        {
          fileReader.close();
        }
      }
      catch (IOException ex) {
        ex.printStackTrace();
      }
    }

    Word[] words = occurenceCounter.getWordPerOccurences();

    System.out.println("Total number of different words : " + words.length);

    Node huffManTree = Node.createHuffmanTree(words);

    huffManTree.printTableCode();

    Hashtable <String, String> codeTable = huffManTree.getEncodedCharacterList();

    writeFileOnDisk(codeTable, path, "output.zz");

    //Debug
    fromFileOnDisk("output.zz", "output2.txt");

  }

  public void fromFileOnDisk(String pathInput, String pathOutput)
  {
    System.out.println("Uncompress file to " + pathOutput);

    //File readers/writers
    FileOutputStream outputWriter = null;
    BufferedReader bufferReader = null;
    FileReader fileReader = null;

    try {

      outputWriter =  new FileOutputStream(pathOutput);
      fileReader = new FileReader(pathInput);
      bufferReader = new BufferedReader(fileReader);

      //Will contain the header size, stored in string
      StringBuilder sizeOfHeaderBuilder = new StringBuilder();
      char[] tmpChar = new char[1];

      //Read the file until a comma is found to get the size of the header
      while(bufferReader.read(tmpChar) != -1)
      {
        if(tmpChar[0] == ',')
        {
          break;
        }
        else
        {
          sizeOfHeaderBuilder.append(tmpChar[0]);
        }
      }

      String sizeOfHeaderString = sizeOfHeaderBuilder.toString();
      System.out.println("Header size : " + sizeOfHeaderString);

      //Convert the size in string to int
      int sizeOfHeaderInt = Integer.parseInt(sizeOfHeaderString);

      StringBuilder headerBuilder = new StringBuilder();

      //Store the header in buffer
      int readReturn;
      for(int i = 0; i < sizeOfHeaderInt; i++)
      {
        readReturn = bufferReader.read(tmpChar);
        if(readReturn == -1)
        {
          System.out.println("Error while reading file");
          return;
        }

        headerBuilder.append(tmpChar[0]);
      }

      String encodedHeader = headerBuilder.toString();

      //Decode the header from Base64 string to binary data, then cast in Hashtable
      Hashtable <String, String> codeTable = null;
      try{
        ByteArrayInputStream bis = new ByteArrayInputStream(Base64.getDecoder().decode(encodedHeader));
        ObjectInputStream ois = new ObjectInputStream(bis);
        codeTable = (Hashtable<String, String>) ois.readObject();
      } catch(Exception e)
      {
        e.printStackTrace();
      }
      finally {
        //Supress a warning
        if(codeTable == null)
        {
          codeTable = null;
        }
      }

      System.out.println("Reconstructing the tree ...");
      Node root = new Node();

      Set<String> keys = codeTable.keySet();

      for(String key : keys)
      {
        String code = codeTable.get(key);
        int codeLength = code.length();

        //Create the nodes

        Node actualNode = root;
        for(int i = 0; i < codeLength; i++)
        {
          if(code.charAt(i) == '0')
          {
            if(actualNode.getLeftChild() == null)
            {
              actualNode.setLeftChild(new Node());
            }
            actualNode = actualNode.getLeftChild();
          }
          else if(code.charAt(i) == '1')
          {
            if(actualNode.getRightChild() == null)
            {
              actualNode.setRightChild(new Node());
            }
            actualNode = actualNode.getRightChild();
          }
        }
        actualNode.setWord(new Word(key));
      }

      System.out.println("Extracting file ");

      //Extracting the file
      char[] readingBuffer = new char[READING_BUFFER_SIZE];
      char[] outputWrittingBuffer = new char[OUTPUT_BUFFER_SIZE];

      int indexInReadingBuffer = 0;
      int indexWrittingBuffer = 0;
      while(bufferReader.read(readingBuffer) != 1)
      {
        for(int i = 0; i < READING_BUFFER_SIZE; i++)
        {

        }
      }

    } catch(IOException e) {
      e.printStackTrace();
    } finally {

      try
      {
        if (bufferReader != null)
        {
          bufferReader.close();
        }
        if (fileReader!= null)
        {
          fileReader.close();
        }
        if(outputWriter != null)
        {
          outputWriter.close();
        }
      }
      catch (IOException ex)
      {
        ex.printStackTrace();
      }
    }
  }

  private void writeFileOnDisk(Hashtable <String, String> codeTable, String pathInput, String pathOutput)
  {

    System.out.println("Opening output file");

    FileOutputStream outputWriter = null;
    BufferedReader bufferReader = null;
    FileReader fileReader = null;

    byte[] outputByteBuffer = new byte[OUTPUT_BUFFER_SIZE];
    int totalBufferSize = 8 * OUTPUT_BUFFER_SIZE;

    StringBuilder outputStringBuffer = new StringBuilder();
    int writedInStringBuffer = 0;

    try {

      outputWriter =  new FileOutputStream(pathOutput);
      fileReader = new FileReader(pathInput);
      bufferReader = new BufferedReader(fileReader);

      String currentLine;
      String code;

      int codeLength;

      System.out.println("Writing Huffman code ");
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream( baos );
      oos.writeObject(codeTable);
      oos.close();

      String hashCodeToString = Base64.getEncoder().encodeToString(baos.toByteArray());

      //First write the length of the tableCode in file
      System.out.println("Size of input header : " + hashCodeToString.length());
      outputWriter.write(Integer.toString(hashCodeToString.length()).getBytes());
      outputWriter.write(',');
      outputWriter.write(hashCodeToString.getBytes());

      System.out.println("Writing data ...");
      while ((currentLine = bufferReader.readLine()) != null) {

        String[] words = currentLine.split("");

        for(String word : words)
        {
          code = codeTable.get(word);
          if(code == null)
          {
            if(word.length() == 0)
            {
              continue;
            }
            else
            {
              System.out.println("The word " + word + " does not exists in the codetable, it is the right one ?");
            }
          }
          codeLength = code.length();
          if(writedInStringBuffer + codeLength > totalBufferSize)
          {
            //Calculate the extra bytes size
            int toWriteInNextBuffer = writedInStringBuffer + codeLength - totalBufferSize;
            outputStringBuffer.append(code.substring(0, codeLength - toWriteInNextBuffer));

            //Convert the buffer to bytes
            String stringToByte = outputStringBuffer.toString();
            for(int i = 0; i < OUTPUT_BUFFER_SIZE ; i++)
            {
              outputByteBuffer[i] = Byte.parseByte( stringToByte.substring(i * 7, (i + 1) * 7), 2);
            }

            //Write the byte buffer
            outputWriter.write(outputByteBuffer);

            //Empty the string buffer and add the extra bytes
            outputStringBuffer = new StringBuilder();
            outputStringBuffer.append(code.substring(toWriteInNextBuffer));
            writedInStringBuffer = toWriteInNextBuffer;
          }
          else
          {
            outputStringBuffer.append(code);
            writedInStringBuffer += codeLength;
          }
        }

      }

      //Append the necessary 0 to the end of the string buffer to
      //have a size divisable by 8
      int toAppendToStringBuffer = writedInStringBuffer  % 8;
      for(int i = 0; i < toAppendToStringBuffer; i++)
      {
        //Could be 0 or 1, we dont care
        outputStringBuffer.append("0");
      }

      String stringToByte = outputStringBuffer.toString();
      int outputStringBufferLength = writedInStringBuffer + toAppendToStringBuffer;
      int totalBytesWritten;

      for(totalBytesWritten = 0; (totalBytesWritten * 8) < outputStringBufferLength;totalBytesWritten++)
      {
        outputByteBuffer[totalBytesWritten] = Byte.parseByte( stringToByte.substring(totalBytesWritten * 7, (totalBytesWritten+ 1) * 7), 2);
      }

      //Write the last bytes in the file
      outputWriter.write(Arrays.copyOfRange(outputByteBuffer,0, totalBytesWritten));

    } catch (IOException e) {

      e.printStackTrace();

    } finally {

      try
      {
        if (bufferReader != null)
        {
          bufferReader.close();
        }
        if (fileReader!= null)
        {
          fileReader.close();
        }
        if(outputWriter != null)
        {
          outputWriter.close();
        }
      }
      catch (IOException ex)
      {
        ex.printStackTrace();
      }
    }

    System.out.println("Ok");

  }

}
