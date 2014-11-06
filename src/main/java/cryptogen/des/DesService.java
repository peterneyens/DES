package cryptogen.des;

public interface DesService {

    public void encryptFile(String filePath, String key);
    
    public void decryptFile(String filePath, String key);

    public void encryptFile(String filePath, byte[][][] subKeys);
    
    public void decryptFile(String filePath, byte[][][] subKeys);
    
}
