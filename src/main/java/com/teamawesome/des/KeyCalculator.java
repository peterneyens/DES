package com.teamawesome.des;

/**
 *
 * @author Nick
 */
public class KeyCalculator {
    //permutatie tabellen
    public static int[] permutatieTabel1C = new int[]{57, 49, 41, 33, 25, 17, 9, 1, 58, 50, 42, 34, 26, 18, 10, 2, 59, 51, 43, 35, 27, 19, 11, 3, 60, 52, 44, 36};
    public static int[] permutatieTabel1D = new int[]{63, 55, 47, 39, 31, 23, 15, 7, 62, 54, 46, 38, 30, 22, 14, 6, 61, 53, 45, 37, 29, 21, 13, 5, 28, 20, 12, 4};
    public static int[] permutatieTabel2 = new int[]{14, 17, 11, 24, 1, 5, 3, 28, 15, 6, 21, 10, 23, 19, 12, 4, 26, 8, 16, 7, 27, 20, 13, 2, 41, 52, 31, 37, 47, 55, 30, 40, 51, 45, 33, 48, 44, 49, 39, 56, 34, 53, 46, 42, 50, 36, 29, 32};
    //aantal iteraties
    public static int[] iteraties = new int[]{1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1};

    // maakt de keys aan
    public byte[][] Generate(byte[] sourceCD, int[] positionsC, int[] positionsD) {
        // Splitst de source array in twee tabellen (C, D)
        byte[] C = ByteHelper.permutate(sourceCD, permutatieTabel1C);
        byte[] D = ByteHelper.permutate(sourceCD, permutatieTabel1D);
        
        //Array om de keys in op te slaan
        byte[][] keys = new byte[7][15];

        for (int i = 0; i < (iteraties.length - 1); i++) {
            //Voer de benodigde left shifts uit
            C = ByteHelper.rotateLeft(C, 28, iteraties[i]);
            D = ByteHelper.rotateLeft(D, 28, iteraties[i]);
            
            //Voeg C en D terug samen
            byte[] CD = new byte[C.length + D.length];
            System.arraycopy(C, 0, CD, 0, C.length);
            keys[i] = CD;          
        }
        
        return keys;
    }
}
