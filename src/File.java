
public class File {

  public File(String path)
  {
    System.out.println("Opening " + path);
  }

  public File compressFile(String path)
  {
    System.out.println("Compressing to " + path);

    File compressedFile = new File(path);

    return compressedFile;
  }
}
