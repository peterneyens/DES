package cryptogen.des;

/**
 * @author Peter.
 */
public abstract class AbstractDesService implements DesService {

    public void encryptFile(String filePath, String key) {
        final byte[][][] subKeys = KeyCalculator.generateFor3Des(key);
        encryptFile(filePath, subKeys);
    }

    public void decryptFile(String filePath, String key) {
        final byte[][][] subKeys = KeyCalculator.generateFor3Des(key);
        final byte[][][] reversedSubKeys = KeyCalculator.reverseSubKeys(subKeys);
        decryptFile(filePath, reversedSubKeys);
    }

    abstract public void encryptFile(String filePath, byte[][][] subKeys);

    abstract public void decryptFile(String filePath, byte[][][] reversedSubKeys); 

}
