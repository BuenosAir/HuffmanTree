import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileOutputStream;
import java.util.Hashtable;
import java.util.Arrays;

public class CompressedFile
{

  public static final int OUTPUT_BUFFER_SIZE = 1000;

  public CompressedFile(String path)
  {
    System.out.println("Opening " + path);

    BufferedReader bufferReader = null;
    FileReader fileReader = null;

    OccurenceCounter occurenceCounter = new OccurenceCounter();

    try {

      fileReader = new FileReader(path);
      bufferReader = new BufferedReader(fileReader);

      String currentLine;

      while ((currentLine = bufferReader.readLine()) != null) {

        String[] words = currentLine.split("");

        for(String word : words)
        {
          occurenceCounter.addWordToCounter(word);
        }

        occurenceCounter.addWordToCounter("\n");
      }

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
      } 
      catch (IOException ex)
      {
        ex.printStackTrace();
      }
    }

    Word[] words = occurenceCounter.getWordPerOccurences();

    System.out.println("Total number of different words : " + words.length);

    Node huffManTree = Node.createHuffmanTree(words);

    huffManTree.printTableCode();

    Hashtable <String, String> codeTable = huffManTree.getEncodedCharacterList();

    exportToCompressedFile(codeTable, path, "output.zz");

  }

  private void exportToCompressedFile (Hashtable <String, String> codeTable, String pathInput, String pathOutput)
  {

    System.out.println("Opening output file");


    FileOutputStream outputWriter = null;
    BufferedReader bufferReader = null;
    FileReader fileReader = null;

    byte[] outputByteBuffer = new byte[OUTPUT_BUFFER_SIZE];
    int totalBufferSize = 8 * OUTPUT_BUFFER_SIZE;

    String outputStringBuffer = new String();
    int writedInStringBuffer = 0;



    try {
      
      outputWriter =  new FileOutputStream(pathOutput);
      fileReader = new FileReader(pathInput);
      bufferReader = new BufferedReader(fileReader);

      String currentLine;
      String code;

      int codeLength;

      //Debug vars 
      int fileLength = 100182;
      int lineNumber = 0;

      while ((currentLine = bufferReader.readLine()) != null) {

        lineNumber++;
        if((lineNumber % (fileLength / 100 ) == 0))
        {
          System.out.println((100 * lineNumber / fileLength));

        }
        String[] words = currentLine.split("");


        for(String word : words)
        {
          code = codeTable.get(word);
          if(code == null)
          {
            System.out.println("The word does not exists in the codetable, it is the right one ?");
            //TODO: Thow exception
            return;
          }
          codeLength = code.length();
          if(writedInStringBuffer + codeLength > totalBufferSize)
          {
            //Calculate the extra bytes size
            int toWriteInNextBuffer = writedInStringBuffer + codeLength - totalBufferSize;
            outputStringBuffer += code.substring(0, codeLength - toWriteInNextBuffer);

            //Convert the buffer to bytes
            for(int i = 0; i < OUTPUT_BUFFER_SIZE ; i++)
            {
              outputByteBuffer[i] = Byte.parseByte( outputStringBuffer.substring(i * 7, (i + 1) * 7), 2);
            }

            //Write the byte buffer
            outputWriter.write(outputByteBuffer);

            //Empty the string buffer and add the extra bytes
            outputStringBuffer = code.substring(toWriteInNextBuffer);
            writedInStringBuffer = toWriteInNextBuffer;
          }
          else 
          {
            outputStringBuffer = outputStringBuffer + code;
            writedInStringBuffer += codeLength;
          }
        }

      }
      
      //Append the necessary 0 to the end of the string buffer to
      //have a size divisable by 8
      int toAppendToStringBuffer = outputStringBuffer.length() % 8;
      for(int i = 0; i < toAppendToStringBuffer; i++)
      {
        //Could be 0 or 1, we dont care
        outputStringBuffer += "0";
      }

      int outputStringBufferLength = outputStringBuffer.length();
      int totalBytesWritten;
      System.out.println(outputStringBufferLength);

      for(totalBytesWritten = 0; (totalBytesWritten * 8) < outputStringBufferLength;totalBytesWritten++)
      {
        System.out.println(totalBytesWritten + " " + outputStringBufferLength);
        outputByteBuffer[totalBytesWritten] = Byte.parseByte( outputStringBuffer.substring(totalBytesWritten * 7, (totalBytesWritten+ 1) * 7), 2);
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
