import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.Hashtable;
import java.util.Set;
import java.io.InputStream;

@SuppressWarnings("unchecked") //We must suppress this warning caused by the import of the Hashtable storing the serialized codeTable, will be corrected by hashing the input file

public class CompressedFile
{
  //public static final int OUTPUT_BUFFER_SIZE = 4 * 5000;

  //public static final int READING_BUFFER_SIZE = 4 * 5000;
  public static final int OUTPUT_BUFFER_SIZE =  8 * 10;
  public static final int READING_BUFFER_SIZE =  8 * 10;

  Hashtable <String, String> codeTable = null;

  //Contains the path to the file to compress
  private String inputPath = null;

  public CompressedFile(String path)
  {
    this.inputPath = path;
  }

  public void createCodeTable()
  {
    if(inputPath == null)
    {
      //TODO: Thow exception
    }

    System.out.println("Opening " + inputPath);

    BufferedReader bufferReader = null;
    FileReader fileReader = null;

    OccurenceCounter occurenceCounter = new OccurenceCounter();

    char[] readingBuffer = new char[READING_BUFFER_SIZE];

    try {

      //Get the file size for the progress bar
      File file = new File(inputPath);
      long fileSize = file.length();

      System.out.println("Filesize : " + fileSize);

      fileReader = new FileReader(inputPath);
      bufferReader = new BufferedReader(fileReader);

      long totalBytesReaden = 0;

      System.out.println("Analysing file ...");

      long timestamp = System.currentTimeMillis();
      long readDuringTwoTimestamp = 0;

      int read = bufferReader.read(readingBuffer);
      while(read != -1)
      {
        for(int i = 0; i < read; i++)
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

        read = bufferReader.read(readingBuffer);
      }

    } catch (IOException e) {

      e.printStackTrace();
      return;

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

    //Add the special "word" that tell that we finished to read the file
    occurenceCounter.addWordToCounter("EOF");

    Word[] words = occurenceCounter.getWordPerOccurences();

    System.out.println("Total number of different words : " + words.length);

    Node huffManTree = Node.createHuffmanTree(words);

    huffManTree.printTableCode();

    //Store the codeTable 
    this.codeTable = huffManTree.getEncodedCharacterList();


  }

  public void decompressFileToDisk(String outputPath)
  {
    System.out.println("Uncompress file to " + outputPath);

    //File readers/writers
    FileOutputStream outputWriter = null;
    InputStream inputStream = null;

    try {

      outputWriter =  new FileOutputStream(outputPath);
      inputStream = new FileInputStream(inputPath);

      //Will contain the header size, stored in string
      StringBuilder sizeOfHeaderBuilder = new StringBuilder();
      byte[] tmpChar = new byte[1];

      //Read the file until a comma is found to get the size of the header
      while(inputStream.read(tmpChar) != -1)
      {
        if((char) tmpChar[0] == ',')
        {
          break;
        }
        else
        {
          sizeOfHeaderBuilder.append((char)tmpChar[0]);
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
        readReturn = inputStream.read(tmpChar);
        if(readReturn == -1)
        {
          System.out.println("Error while reading file");
          return;
        }

        headerBuilder.append((char)tmpChar[0]);
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
      Node actualNode = null;

      Set<String> keys = codeTable.keySet();

      for(String key : keys)
      {
        String code = null;
        try {
          code = codeTable.get(key);
        }
        catch(Exception e)
        {
          System.out.println("Symbol " + code + " not found in codeTable");
          e.printStackTrace();
        }

        //Must always be 1 except if it is EOF
        int codeLength = code.length();

        //Create the nodes
        actualNode = root;
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

      System.out.println("Extracting file");

      //Extracting the file
      byte[] readingBuffer = new byte[READING_BUFFER_SIZE];
      byte[] outputWrittingBuffer = new byte[OUTPUT_BUFFER_SIZE];

      int indexInReadingBuffer = 0;
      int indexWrittingBuffer = 0;

      StringBuilder binaryRepresentationBuilder = new StringBuilder();
      StringBuilder outputStringBufferBuilder = new StringBuilder();

      //Vars that will store the temporary words and strings
      Word tmpWord;
      String tmpString;
      int tmpStringLength;

      actualNode = root;

      root.printTableCode();

      int read = inputStream.read(readingBuffer);
      while(read != -1)
      {
        binaryRepresentationBuilder = new StringBuilder();
        for(int i = 0; i < read; i++)
        {
         //Convert the reading buffer in bit representation
          binaryRepresentationBuilder.append(String.format("%7s", Integer.toBinaryString(readingBuffer[i] & 0xFF)).replace(' ', '0'));
        }

        String binaryRepresentation = binaryRepresentationBuilder.toString();

        int  binaryRepresentationLength = binaryRepresentation.length();

        for(int u = 0; u < binaryRepresentationLength; u++)
        {
          if(binaryRepresentation.charAt(u) == '0')
          {
            actualNode = actualNode.getLeftChild();
          }
          else
          {
            actualNode = actualNode.getRightChild();
          }

          if(actualNode == null)
          {
            System.out.println("Error");
            return;
          }
          //If it is a leaf
          if(actualNode.getLeftChild() == null && actualNode.getRightChild() == null)
          {
            tmpWord = actualNode.getWord();
            tmpString = tmpWord.getContent();
            tmpStringLength = tmpString.length();

            System.out.print(tmpString);

            if(tmpString.equals("EOF"))
            {
              System.out.println("End of file reached, returning");

              break;
            }
            //if(indexWrittingBuffer + tmpStringLength > OUTPUT_BUFFER_SIZE)
            //{
             //outputStringBufferBuilder.append(tmpString.substring(0, OUTPUT_BUFFER_SIZE - indexWrittingBuffer));
             //String outputStringBuffer = outputStringBufferBuilder.toString();

             //System.out.println(outputStringBuffer);

             ////Convert the string to bytes

             ////Write file to disk

            //}
            //else
            //{
              //outputStringBufferBuilder.append(tmpString);
            //}

             actualNode = root;
          }
        }
        read = inputStream.read(readingBuffer);
      }
        System.out.println("Ok");

    } catch(IOException e) {
      e.printStackTrace();
    } finally {

      try
      {
        if (inputStream != null)
        {
          inputStream.close();
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

  public void compressFileToDisk(String outputPath)
  {

    if(codeTable == null)
    {
      System.out.println("The file has not been correcty initalized, doing it first");
      this.createCodeTable();
    }
    
    if(outputPath == null)
    {
      //TODO: Throw exception
    }

    System.out.println("Opening output file");

    FileOutputStream outputWriter = null;
    BufferedReader bufferReader = null;
    FileReader fileReader = null;

    byte[] outputByteBuffer = new byte[OUTPUT_BUFFER_SIZE];
    int totalBufferSize = 7 * OUTPUT_BUFFER_SIZE;

    char[] readingBuffer = new char[READING_BUFFER_SIZE];

    StringBuilder outputStringBuffer = new StringBuilder();
    int writedInStringBuffer = 0;

    try {

      outputWriter =  new FileOutputStream(outputPath);
      fileReader = new FileReader(inputPath);
      bufferReader = new BufferedReader(fileReader);

      String currentLine = null;
      String code = null;

      int codeLength;

      System.out.println("Writing Huffman code ");
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream( baos );
      oos.writeObject(codeTable);
      oos.close();

      String hashCodeToString = Base64.getEncoder().encodeToString(baos.toByteArray());

      System.out.println(hashCodeToString);

      //First write the length of the tableCode in file
      System.out.println("Size of input header : " + hashCodeToString.length());
      outputWriter.write(Integer.toString(hashCodeToString.length()).getBytes());
      outputWriter.write(',');
      outputWriter.write(hashCodeToString.getBytes());

      System.out.println("Writing data ...");

      //TODO: To remove
      int nullcounter = 0;
      boolean hasBeenPrinted = false;

      int read = bufferReader.read(readingBuffer);

      while (read != -1) {

        for(int v = 0; v < read; v++)
        {
          String word = String.valueOf(readingBuffer[v]);
          System.out.print(readingBuffer[v]);

          try{
            code = codeTable.get(word);
          }
          catch(Exception e)
          {
            System.out.println("Symbol " + word + " not found in codeTable");
            e.printStackTrace();
          }

          //Must always return 1
          codeLength = code.length();

          if(writedInStringBuffer + codeLength > totalBufferSize)
          {
            System.out.println("\n");
            System.out.println("A " + writedInStringBuffer + " " + outputStringBuffer.toString().length() + " " + codeLength + " " + totalBufferSize);
            //Calculate the extra bytes size

            int excess = writedInStringBuffer + codeLength - totalBufferSize;
            System.out.println("Excess : " + excess);
            outputStringBuffer.append(code.substring(0, codeLength - excess));

            //Convert the buffer to bytes
            String stringToByte = outputStringBuffer.toString();

            int i;
            for(i = 0; (i + 1) * 7 < totalBufferSize; i++)
            {
              outputByteBuffer[i] = Byte.parseByte( stringToByte.substring(i * 7, (i + 1) * 7), 2);
            }

            System.out.println("Last byte is : " + stringToByte.substring(i * 7, (i + 1) * 7));

            System.out.println(i + " " + totalBufferSize + " " + stringToByte.length());
            //Write the byte buffer
            outputWriter.write(outputByteBuffer);

            //Empty the string buffer and add the extra bytes
            outputStringBuffer = new StringBuilder();
            outputStringBuffer.append(code.substring(codeLength - excess));
            writedInStringBuffer = excess;
          }
          else
          {
            outputStringBuffer.append(code);
            writedInStringBuffer += codeLength;
          }
        }

        read = bufferReader.read(readingBuffer);
      }

      //Add the EOF symbol to the buffer
      String endOfFile = null;
      try {
        endOfFile = codeTable.get("EOF");
      }
      catch(Exception e)
      {
        System.out.println("EOF symbol not found in table");
        e.printStackTrace();
      }

      outputStringBuffer.append(endOfFile);

      //Append the necessary 0 to the end of the string buffer to
      //have a size divisable by 7
      int toAppendToStringBuffer = writedInStringBuffer  % 7;
      for(int i = 0; i < toAppendToStringBuffer; i++)
      {
        //Could be 0 or 1, we dont care
        outputStringBuffer.append("0");
      }

      String stringToByte = outputStringBuffer.toString();
      System.out.println("Finally writing " + stringToByte + " of length " + stringToByte.length());
      //int outputStringBufferLength = writedInStringBuffer + toAppendToStringBuffer;
      int outputStringBufferLength = stringToByte.length();
      int totalBytesWritten;

      for(totalBytesWritten = 0; (totalBytesWritten + 1) * 7 < outputStringBufferLength; totalBytesWritten++)
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
