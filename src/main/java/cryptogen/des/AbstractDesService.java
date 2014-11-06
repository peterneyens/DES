package cryptogen.des;

import helpers.ByteHelper;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

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
        byte[][][] reversedSubKeys = reverseSubKeys(subKeys);

        decryptFile(filePath, reversedSubKeys);
    }

    abstract public void encryptFile(String filePath, byte[][][] subKeys);

    abstract public void decryptFile(String filePath, byte[][][] reversedSubKeys); 

    private static byte[][][] reverseSubKeys(byte[][][] subKeys) {
        byte[][][] reversedSubKeys = new byte[subKeys.length][][];
        for (int i = 0; i < subKeys.length; i++) {
            int reversedI = subKeys.length - 1 - i;
            reversedSubKeys[reversedI] = new byte[subKeys[i].length][];
            for (int j = 0; j < subKeys[i].length; j++) {
                int reversedJ = subKeys[i].length - 1 - j;
                reversedSubKeys[reversedI][reversedJ] = subKeys[i][j];
            }
        }
        return reversedSubKeys;
    }

}
