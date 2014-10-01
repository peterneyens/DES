package cryptogen;

import helpers.ByteHelper;

import java.util.Arrays;

/**
 *
 * @author Nick
 */
public class KeyCalculator {

    //permutatie tabellen

    public static int[] permutatieTabel1 = new int[]{
        57, 49, 41, 33, 25, 17, 9,
        1, 58, 50, 42, 34, 26, 18,
        10, 2, 59, 51, 43, 35, 27,
        19, 11, 3, 60, 52, 44, 36,
        63, 55, 47, 39, 31, 23, 15,
        7, 62, 54, 46, 38, 30, 22,
        14, 6, 61, 53, 45, 37, 29,
        21, 13, 5, 28, 20, 12, 4
    };
    public static int[] permutatieTabel2 = new int[]{
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
    public static int[] iteraties = new int[]{1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1};

    // maakt de keys aan
    public static byte[][] Generate(byte[] sourceCD) {
        
        // Voer permutatie matrix uit
        byte[] permutatedBlock = ByteHelper.permutate(sourceCD, permutatieTabel1);
        
        // Splits de source array in twee tabellen (C, D)
        byte[] C = KeyCalculator.selectBits(permutatedBlock, 0, 28);
        byte[] D = KeyCalculator.selectBits(permutatedBlock, 28, 28);

        System.out.println("permutatedBlock nb bytes " + permutatedBlock.length); // 7 bytes
        System.out.println("C nb bytes " + C.length); // now 7 / 2 - 1   => 3 - 1 = 2    ==> 2 bytes
        System.out.println("D nb bytes " + D.length); // 4 bytes

        //Array om de keys in op te slaan
        byte[][] keys = new byte[16][6];

        for (int i = 0; i < iteraties.length; i++) {
//            ByteHelper.printBytesAsBits(C);
//            ByteHelper.printBytesAsBits(D);

            //Voer de benodigde left shifts uit
            C = ByteHelper.rotateLeft(C, 28, iteraties[i]);
            D = ByteHelper.rotateLeft(D, 28, iteraties[i]);

            //Voeg C en D terug samen
//            byte[] CD = new byte[C.length + D.length];
//            System.arraycopy(C, 0, CD, 0, C.length);
//            System.arraycopy(D, 0, CD, D.length, D.length);
             byte[] CD = concatenateBits(C,28,D,28);
            keys[i] = ByteHelper.permutate(CD, permutatieTabel2);
        }

        return keys;
    }

    public static byte[] selectBits(byte[] in, int pos, int len) {
        int numOfBytes = (len - 1) / 8 + 1;
        byte[] out = new byte[numOfBytes];
        for (int i = 0; i < len; i++) {
            int val = ByteHelper.getBit(in, pos + i);
            ByteHelper.setBit(out, i, val);
        }
        return out;
    }
    
    private static byte[] concatenateBits(byte[] a, int aLen, byte[] b, int bLen) {
      int numOfBytes = (aLen+bLen-1)/8 + 1;
      byte[] out = new byte[numOfBytes];
      int j = 0;
      for (int i=0; i<aLen; i++) {
         int val = ByteHelper.getBit(a,i);
         ByteHelper.setBit(out,j,val);
         j++;
      }
      for (int i=0; i<bLen; i++) {
         int val = ByteHelper.getBit(b,i);
         ByteHelper.setBit(out,j,val);
         j++;
      }
      return out;
   }
}
