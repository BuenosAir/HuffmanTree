public class Main {

  public static void main(String []args)
  {
    int argsLength = args.length;
    if(argsLength == 0 || args[0].equals("--help"))
    {
      System.out.println("--help : print this helper");
      System.out.println("--compress inputPath outputPath");
      System.out.println("--decompress inputPath outputPath");
      return;
    }
    else if(args[0].equals("--test"))
    {
      System.out.println("Testing all functionnality");
      CompressedFile compressedFile = new CompressedFile("./bible.txt");

      compressedFile.createCodeTable();
      compressedFile.compressFileToDisk("output.zz");

      CompressedFile decompressedFile = new CompressedFile("output.zz");
      decompressedFile.decompressFileToDisk("decompressed.txt");
    }
    else if(args[0].equals("--compress"))
    {
      if(argsLength < 3)
      {
        System.out.println("Missings arguments");
        return;
      }

      CompressedFile compressedFile = new CompressedFile(args[1]);
      compressedFile.createCodeTable();
      compressedFile.compressFileToDisk(args[2]);
      //TODO: Check if the file has been correctly compressed
    }
    else if(args[0].equals("--decompress"))
    {
      if(argsLength < 3)
      {
        System.out.println("Missing arguments");
        return;
      }

      CompressedFile decompressedFile = new CompressedFile(args[1]);
      decompressedFile.decompressFileToDisk(args[2]);
      //TODO: Check if the file has been correctyle uncompressed
    }
  }
}


