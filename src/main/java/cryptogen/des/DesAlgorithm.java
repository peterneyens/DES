package cryptogen.des;

import helpers.ByteHelper;
import java.util.Arrays;

/**
 * DesAlgorithm includes methods to encrypt and decrypt blocks of data.
 * If you supply more than one sequence of subkeys, the blocks are encrypted or decrypted
 * multiple types.
 * eg. for 3DES, supply 3 sequences of subkeys.
 *
 * @author Peter.
 */
public class DesAlgorithm {

    /**
     * The number of bytes in one block.
     */
    public static final int blockSizeInBytes = 8;

    // IP matrix
    private static final int[] initialPermutation = new int[] {
        58, 50, 42, 34, 26, 18, 10, 2,
        60, 52, 44, 36, 28, 20, 12, 4,
        62, 54, 46, 38, 30, 22, 14, 6,
        64, 56, 48, 40, 32, 24, 16, 8,
        57, 49, 41, 33, 25, 17, 9, 1,
        59, 51, 43, 35, 27, 19, 11, 3,
        61, 53, 45, 37, 29, 21, 13, 5,
        63, 55, 47, 39, 31, 23, 15, 7
    };

    // IP-1 matrix
    private static final int[] inverseInitialPermutation = new int[] {
        40, 8, 48, 16, 56, 24, 64, 32,
        39, 7, 47, 15, 55, 23, 63, 31,
        38, 6, 46, 14, 54, 22, 62, 30,
        37, 5, 45, 13, 53, 21, 61, 29,
        36, 4, 44, 12, 52, 20, 60, 28,
        35, 3, 43, 11, 51, 19, 59, 27,
        34, 2, 42, 10, 50, 18, 58, 26,
        33, 1, 41, 9, 49, 17, 57, 25
    };

    /**
     * Encrypt the specified block multiple times based on the length of the specified subkeys.
     * 
     * @param block the block of data to be encrypted
     * @param subkeys the subkeys used to encrypt the block
     */
    public static byte[] encryptBlock(byte[] block, byte[][][] subKeys) throws IllegalArgumentException {
        byte[] tempBlock = block;
        for (int i = 0; i < subKeys.length; i++) {
            tempBlock = encryptBlock(tempBlock, subKeys[i]);
        }
        return tempBlock;
    }

    /**
     * Encrypt the specified block one time using the specified subkeys.
     * 
     * @param block the block of data to be encrypted
     * @param subkeys the subkeys used to encrypt the block
     */
    public static byte[] encryptBlock(byte[] block, byte[][] subKeys) throws IllegalArgumentException {
        if (block.length != 8)
            throw new IllegalArgumentException("Block not 8 length");

        final byte[] permutatedBlock = ByteHelper.permutFunc(block, initialPermutation);

        byte[] prevLeft, prevRight, left, right;
        // verdeel in initiele linkse en rechtse blok
        prevLeft = ByteHelper.getFirstHalf(permutatedBlock);
        prevRight = ByteHelper.getSecondHalf(permutatedBlock);

        // bereken L1 R1 tem L16 R16
        for (int i = 1; i <= 16; i++) {

            // bereken linkse en rechtse blok
            left = prevRight;
            right = ByteHelper.xorByteBlocks(prevLeft, Feistel.executeFunction(prevRight, subKeys[i - 1]));

            // voorbereiding volgende iteratie
            prevLeft = left;
            prevRight = right;
        }

        // swap voor laatste iteratie
        left = prevRight;
        right = prevLeft;

        return ByteHelper.permutFunc(ByteHelper.concatBlocks(left, right), inverseInitialPermutation);
    }

 
     /**
     * Decrypt the specified block multiple times based on the length of the specified subkeys.
     * 
     * @precondition the subkeys should the reversed subkeys used to encrypt.
     * @param block the block of data to be decrypted
     * @param subkeys the subkeys used to decrypt the block
     */
    public static byte[] decryptBlock(byte[] block, byte[][][] reversedSubKeys) throws IllegalArgumentException {
        return encryptBlock(block, reversedSubKeys);
    }

     /**
     * Decrypt the specified block one time using the specified subkeys.
     * 
     * @precondition the subkeys should the reversed subkeys used to encrypt.
     * @param block the block of data to be decrypted
     * @param subkeys the subkeys used to decrypt the block
     */
    public static byte[] decryptBlock(byte[] block, byte[][] reversedSubKeys) throws IllegalArgumentException {
        return encryptBlock(block, reversedSubKeys);
    }

}
