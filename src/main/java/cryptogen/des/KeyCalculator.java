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


    // returns an array of subkeys (a subkey is a byte array)
    public static byte[][][] generate(String key) {
        //byte[] keyInBytes = Arrays.copyOfRange(createHash(key), 0, 8);
        //return new byte[][][] { generateSubKeys(keyInBytes) };
        return generateForNDes(key, 1);
    }

    public static byte[][][] generateFor3Des(String key) {
        return generateForNDes(key, 3);
    }

    public static byte[][][] generateForNDes(String key, int nDes) {
        byte[] hash = createHash(key);
      
        if (nDes > (hash.length / 8)) 
            throw new IllegalArgumentException("nDes to big for hashed key");

        byte[][][] subKeysNDes = new byte[nDes][][];
        for (int i = 0; i < nDes; i++) {
            subKeysNDes[i] = generateSubKeys(Arrays.copyOfRange(hash, i, i + 8));
        }

        return subKeysNDes;
    }

    private static byte[] createHash(String text) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(text.getBytes(Charset.forName("UTF-8")));
        } catch (java.security.NoSuchAlgorithmException ex) { 
            System.out.println(ex.getMessage());
            return null;
        }
    }

    
    // maakt de keys aan
    public static byte[][] generateSubKeys(byte[] sourceCD) {

        // Splitst de source array in twee tabellen (C, D)
        //byte[] permutatedBlock = ByteHelper.permutate(sourceCD, permutatieTabel1);
        byte[] permutatedBlock = ByteHelper.permutFunc(sourceCD, permutatieTabel1);

        byte[] C = Arrays.copyOfRange(permutatedBlock, 0,(int) Math.ceil(permutatedBlock.length / 2.0));
        byte[] D = Arrays.copyOfRange(permutatedBlock, permutatedBlock.length / 2, permutatedBlock.length);
        D = ByteHelper.rotateLeft(D, 32, 4); // move 4 bits to left

        //Array om de keys in op te slaan
        byte[][] keys = new byte[16][6];

        // bereken alle subkeys
        for (int i = 0; i < iteraties.length; i++) {

            //Voer de benodigde left shifts uit
            C = ByteHelper.rotateLeft(C, 28, iteraties[i]);
            D = ByteHelper.rotateLeft(D, 28, iteraties[i]);

            //Voeg C en D terug samen
            byte[] CD = new byte[7];
            // get 28 first bits from C
            for(int bit = 0; bit < 28; bit++) {
                ByteHelper.setBit(CD, bit, ByteHelper.getBitInt(C, bit));
            }
            // get 28 next bits from D
            for(int bit = 28; bit < 56; bit++) {
                ByteHelper.setBit(CD, bit, ByteHelper.getBitInt(D, bit - 28));
            }

            keys[i] = ByteHelper.permutFunc(CD, permutatieTabel2);
        }

        // TODO check keyInBytes niet 00000000 of 111111111, of subkeys vaak gelijk, ...

        return keys;
    }
}
