package cryptogen.des;

import helpers.ByteHelper;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.security.MessageDigest;

/**
 *
 * @author Nick, Peter
 */
public class KeyCalculator {

    public static final int[] permutatieTabel1 = new int[]{
            57, 49, 41, 33, 25, 17, 9,
            1, 58, 50, 42, 34, 26, 18,
            10, 2, 59, 51, 43, 35, 27,
            19, 11, 3, 60, 52, 44, 36,

            63, 55, 47, 39, 31, 23, 15,
            7, 62, 54, 46, 38, 30, 22,
            14, 6, 61, 53, 45, 37, 29,
            21, 13, 5, 28, 20, 12, 4
    };

    public static final int[] permutatieTabel2 = new int[]{
            14, 17, 11, 24, 1, 5,
            3, 28, 15, 6, 21, 10,
            23, 19, 12, 4, 26, 8,
            16, 7, 27, 20, 13, 2,
            41, 52, 31, 37, 47, 55,
            30, 40, 51, 45, 33, 48,
            44, 49, 39, 56, 34, 53,
            46, 42, 50, 36, 29, 32
    };

    //aantal iteraties
    private static int[] iteraties = new int[]{1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1};


    /**
     * Generate the subkeys for a DES encryption using the specified key.
     *
     * @param key string for which the subkeys will be generated
     * @return an array of length 1 with an array of subkeys (a subkey is a byte array)
     */
    public static byte[][][] generate(String key) {
        return generateForNDes(key, 1);
    }

    /**
     * Generate the subkeys for a 3DES encryption using the specified key.
     *
     * @param key string for which the subkeys will be generated
     * @return an array of length 3 with arrays of subkeys (a subkey is a byte array)
     */
    public static byte[][][] generateFor3Des(String key) {
        return generateForNDes(key, 3);
    }

    /**
     * Generate n subkeys for an n-DES encryption using the specified key.
     *
     * @param key string for which the subkeys will be generated
     * @param nDes number of arrays with subkeys
     * @return an array of length n with n arrays of subkeys (a subkey is a byte array)
     */
    public static byte[][][] generateForNDes(String key, int nDes) {
        // hash the master key to create longer keys and to minimalize the chance
        // of weak keys and as a consequence a weak encryption
        byte[] hash = createHash(key);
      
        // nDes cannot be too big
        // every generation of subkeys needs a key of 8 bytes
        nDes =  Math.max(nDes, (hash.length / 8)); 

        //  generate nDes times 16 subkeys
        byte[][][] subKeysNDes = new byte[nDes][][];
        for (int i = 0; i < nDes; i++) {
            subKeysNDes[i] = generateSubKeys(Arrays.copyOfRange(hash, i, i + 8));
        }

        return subKeysNDes;
    }

    /**
     * Return a byte array of the hashed specified text.
     *
     * @param text the text to be hashed.
     * @return byte[] byte array of the hashed text.
     */
    private static byte[] createHash(String text) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(text.getBytes(Charset.forName("UTF-8")));
        } catch (java.security.NoSuchAlgorithmException ex) { 
            System.out.println(ex.getMessage());
            return null;
        }
    }

    /**
     * Generate the subkeys for the specified key.
     *
     * @param key the key to generate the subkeys.
     * @return byte[][] the subkeys for the specified key.
     */
    public static byte[][] generateSubKeys(byte[] key) {
        byte[] sourceCD = key;

        // Splitst de source array in twee tabellen (C, D)
        byte[] permutatedBlock = ByteHelper.permutFunc(sourceCD, permutatieTabel1);
        byte[] C = getFirstHalf(permutatedBlock);
        byte[] D = getSecondHalf(permutatedBlock);

        //Array om de keys in op te slaan
        byte[][] keys = new byte[16][6];

        // bereken alle subkeys
        for (int i = 0; i < iteraties.length; i++) {
            //Voer de benodigde left shifts uit
            C = ByteHelper.rotateLeft(C, 28, iteraties[i]);
            D = ByteHelper.rotateLeft(D, 28, iteraties[i]);

            //Voeg C en D terug samen
            byte[] CD = combineCD(C, D); 

            keys[i] = ByteHelper.permutFunc(CD, permutatieTabel2);
        }

        // TODO check key niet 00000000 of 111111111, of subkeys vaak gelijk, ...
        return keys;
    }

    /**
     * Get the first half of the specified block.
     * The significant bits are on the left.
     * eg. when a block with an odd length is split in half, only the first 4 bits
     * of the last byte are significant.
     *
     * @param block the block to be split in half
     * @return byte[] the first half of the specified block
     */
    private static byte[] getFirstHalf(byte[] block) {
        return Arrays.copyOfRange(block, 0, (int) Math.ceil(block.length / 2.0));
    }

    /**
     * Get the second half of the specified block.
     * The significant bits are on the left.
     * eg. when a block with an odd length is split in half, only the first 4 bits
     * of the last byte are significant.
     *
     * @param block the block to be split in half
     * @return byte[] the first half of the specified block
     */
    private static byte[] getSecondHalf(byte[] block) {
        byte[] temp = Arrays.copyOfRange(block, block.length / 2, block.length);
        // middle of block is in the middle of a byte
        if ( (block.length / 2d) % 1 == 0.5) {
            temp = ByteHelper.rotateLeft(temp, temp.length * 8, 4);
        }
        return temp;
    }

    /**
     * Combine blocks C and D.
     *
     * @param C
     * @param C
     * @return CD
     */
    private static byte[] combineCD(byte[] C, byte[] D) {
        byte[] CD = new byte[7];
        // get 28 first bits from C
        for(int bit = 0; bit < 28; bit++) {
            ByteHelper.setBit(CD, bit, ByteHelper.getBitInt(C, bit));
        }
        // get 28 next bits from D
        for(int bit = 28; bit < 56; bit++) {
            ByteHelper.setBit(CD, bit, ByteHelper.getBitInt(D, bit - 28));
        }
        return CD;
    }

    /**
     * Reverse the specified subkeys for decryption.
     * The subkeys are reversed in the first and the second dimension.
     *
     * @param subKeys the subkey which need to be reversed to use with DES decryption.
     * @return byte[][][] the reversed subkeys that can be used for decryption.
     */
    public static byte[][][] reverseSubKeys(byte[][][] subKeys) {
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
