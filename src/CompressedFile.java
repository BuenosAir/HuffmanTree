import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileOutputStream;
import java.util.Hashtable;
import java.util.Arrays;
import java.io.File;

public class CompressedFile
{

  public static final int OUTPUT_BUFFER_SIZE = 100000;
  
  public static final int READING_BUFFER_SIZE = 100000;


  public CompressedFile(String path)
  {
    System.out.println("Opening " + path);

    BufferedReader bufferReader = null;
    FileReader fileReader = null;

    OccurenceCounter occurenceCounter = new OccurenceCounter();

    char[] readingBuffer = new char[READING_BUFFER_SIZE + 100];

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

      while(bufferReader.read(readingBuffer) != -1 ){

        String[] words = String.valueOf(readingBuffer).split("");

        for(String word : words)
        {
          occurenceCounter.addWordToCounter(word);
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

    StringBuilder outputStringBuffer = new StringBuilder();
    int writedInStringBuffer = 0;



    try {
      
      outputWriter =  new FileOutputStream(pathOutput);
      fileReader = new FileReader(pathInput);
      bufferReader = new BufferedReader(fileReader);

      String currentLine;
      String code;

      int codeLength;

      while ((currentLine = bufferReader.readLine()) != null) {

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
