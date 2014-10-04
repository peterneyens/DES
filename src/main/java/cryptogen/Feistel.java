/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cryptogen;

import helpers.ByteArrayBitIterable;
import helpers.ByteHelper;

import java.util.Arrays;

/**
 *
 * @author Arno, Peter
 */
public class Feistel {
    
    public static int[] expansionTabel42Bits = new int[]{
        32, 1, 2, 3, 4, 5,
        4, 5, 6,  7, 8, 9,
        8, 9, 10, 11, 12, 13,
        12, 13, 14, 15, 16, 17,
        16, 17, 18, 19, 20, 21,
        20, 21, 22, 23, 24, 25,
        24, 25, 26, 27, 28, 29,
        28, 29, 30, 31, 32, 1
    };

    // gebruikt door S functie
    public static final int[][] s = {
    {
        14, 4, 13, 1, 2, 15, 11, 8, 3, 10, 6, 12, 5, 9, 0, 7,
        0, 15, 7, 4, 14, 2, 13, 1, 10, 6, 12, 11, 9, 5, 3, 8,
        4, 1, 14, 8, 13, 6, 2, 11, 15, 12, 9, 7, 3, 10, 5, 0,
        15, 12, 8, 2, 4, 9, 1, 7, 5, 11, 3, 14, 10, 0, 6, 13
    },
    {
        15, 1, 8, 14, 6, 11, 3, 4, 9, 7, 2, 13, 12, 0, 5, 10,
        3, 13, 4, 7, 15, 2, 8, 14, 12, 0, 1, 10, 6, 9, 11, 5,
        0, 14, 7, 11, 10, 4, 13, 1, 5, 8, 12, 6, 9, 3, 2, 15,
        13, 8, 10, 1, 3, 15, 4, 2, 11, 6, 7, 12, 0, 5, 14, 9
    },
    {
        10, 0, 9, 14, 6, 3, 15, 5, 1, 13, 12, 7, 11, 4, 2, 8,
        13, 7, 0, 9, 3, 4, 6, 10, 2, 8, 5, 14, 12, 11, 15, 1,
        13, 6, 4, 9, 8, 15, 3, 0, 11, 1, 2, 12, 5, 10, 14, 7,
        1, 10, 13, 0, 6, 9, 8, 7, 4, 15, 14, 3, 11, 5, 2, 12
    },
    {
        7, 13, 14, 3, 0, 6, 9, 10, 1, 2, 8, 5, 11, 12, 4, 15,
        13, 8, 11, 5, 6, 15, 0, 3, 4, 7, 2, 12, 1, 10, 14, 9,
        10, 6, 9, 0, 12, 11, 7, 13, 15, 1, 3, 14, 5, 2, 8, 4,
        3, 15, 0, 6, 10, 1, 13, 8, 9, 4, 5, 11, 12, 7, 2, 14
    },
    {
        2, 12, 4, 1, 7, 10, 11, 6, 8, 5, 3, 15, 13, 0, 14, 9,
        14, 11, 2, 12, 4, 7, 13, 1, 5, 0, 15, 10, 3, 9, 8, 6,
        4, 2, 1, 11, 10, 13, 7, 8, 15, 9, 12, 5, 6, 3, 0, 14,
        11, 8, 12, 7, 1, 14, 2, 13, 6, 15, 0, 9, 10, 4, 5, 3
    },
    {
        12, 1, 10, 15, 9, 2, 6, 8, 0, 13, 3, 4, 14, 7, 5, 11,
        10, 15, 4, 2, 7, 12, 9, 5, 6, 1, 13, 14, 0, 11, 3, 8,
        9, 14, 15, 5, 2, 8, 12, 3, 7, 0, 4, 10, 1, 13, 11, 6,
        4, 3, 2, 12, 9, 5, 15, 10, 11, 14, 1, 7, 6, 0, 8, 13
    },
    {
        4, 11, 2, 14, 15, 0, 8, 13, 3, 12, 9, 7, 5, 10, 6, 1,
        13, 0, 11, 7, 4, 9, 1, 10, 14, 3, 5, 12, 2, 15, 8, 6,
        1, 4, 11, 13, 12, 3, 7, 14, 10, 15, 6, 8, 0, 5, 9, 2,
        6, 11, 13, 8, 1, 4, 10, 7, 9, 5, 0, 15, 14, 2, 3, 12
    },
    {
        13, 2, 8, 4, 6, 15, 11, 1, 10, 9, 3, 14, 5, 0, 12, 7,
        1, 15, 13, 8, 10, 3, 7, 4, 12, 5, 6, 11, 0, 14, 9, 2,
        7, 11, 4, 1, 9, 12, 14, 2, 0, 6, 10, 13, 15, 3, 5, 8,
        2, 1, 14, 7, 4, 10, 8, 13, 15, 12, 9, 0, 3, 5, 6, 11
    }};
    
    public static int[] permutatieTabel32Bits = new int[]{
        16, 7, 20, 21,
        29, 12, 28, 17,
        1, 15, 23, 26,
        5, 18, 31, 10,
        2, 8, 24, 14,
        32, 27, 3, 9,
        19, 13, 30, 6,
        22, 11, 4, 25
    };
    
    /* 
    
    Executes cipher function
    
    INPUT
    block -  32bits - 4bytes
    subkey - 48bits - 6bytes
    
    */
    public static byte[] executeFunction(byte[] block, byte[] subkey) throws IllegalArgumentException {
        
        if (block.length != 4)
            throw new IllegalArgumentException("Block should be 4 bytes long.");
        else if (subkey.length != 6)
            throw new IllegalArgumentException("Key should be 6 bytes long.");

        //System.out.println();
        //System.out.println("subkey");
        //ByteHelper.printByteArray(subkey);

        //long millis1 = System.nanoTime();
        byte[] transmutedBlock = ByteHelper.permutFunc(block, Feistel.expansionTabel42Bits);
        //System.out.println("expanded");
        //long millis2 = System.nanoTime();
        //System.out.println(" -> " + (millis2 - millis1));
        //ByteHelper.printByteArray(transmutedBlock);

        byte[] xoredBytes = ByteHelper.xorByteBlocks(transmutedBlock, subkey);
        //System.out.println("xored");
        //long millis3 = System.nanoTime();
        //System.out.println(" -> " + (millis3 - millis2));
        //ByteHelper.printByteArray(xoredBytes);

        byte[] result = executeS(xoredBytes);
        //System.out.println("result S boxes");
        //long millis4 = System.nanoTime();
        //System.out.println(" -> " + (millis4 - millis3));
        //ByteHelper.printByteArray(result);

        byte[] feistelResult = ByteHelper.permutFunc(result, Feistel.permutatieTabel32Bits);
        //System.out.println("result feistel");
        //long millis5 = System.nanoTime();
        //System.out.println(" -> " + (millis5 - millis4));
        //ByteHelper.printByteArray(feistelResult);

        return feistelResult;
    }

    private static byte[] executeS(byte[] block) {

        byte[] helperBlock = splitInSextets(block);

        // execute S function on each byte
        // each resulting byte contains 4 bits
        for (int i = 0; i < helperBlock.length; i++) {
            helperBlock[i] = S(helperBlock[i], Feistel.s[i]);
        }

        return joinQuartetsToBytes(helperBlock);
    }

    /**
     * Split the block / byte array in bytes with 6 bits.
     *
     * @param block
     * @return
     */
    private static byte[] splitInSextets(byte[] block) {
        byte[] copy = block;//Arrays.copyOf(block, block.length);

        final int nbBits = copy.length * 8;
        final int nbSextets = nbBits / 6;
        byte[] sextets = new byte[nbSextets];

        for(int i = 0; i < nbSextets; i++) {
            // verplaats twee posities naar rechts en maak eerste twee bits 0
            sextets[i] = (byte) ((copy[0] >> 2) & ~(3 << 6));
            // zet volgende sextet vanvoor
            copy = shift6Left(copy);
        }

        return sextets;
    }

    /**
     * Shift all the bits of the byte array 6 positions to the left.
     *
     * @param bytes byte array
     * @return
     */
    private static byte[] shift6Left(byte[] bytes) {

        for (int i = 0; i < bytes.length; i++) {
            // verplaats laatste 2 bits naar voren
            byte newByte = (byte) (bytes[i] << 6);
            if ((i + 1) < bytes.length) {
                // 6 volgende bits (van volgende byte) naar achteren verplaatsen en eerste bits 0 maken
                byte lastSixBits = (byte) ((bytes[i + 1] >> 2) & ~((2+1) << 6));
                // voeg laatste 6 bits toe aan eerste twee bits
                newByte |= lastSixBits;
            }
            bytes[i] = newByte;
        }

        return bytes;
    }

    /* Implementeert S functie
    
    INPUT
    block        -- byte met 6 bits
    matrix       -- matrix van mogelijke getallen
    
    */
    private static byte S(byte block, int[] positions) {

        // neem eesrste en zesde bit en verander naar int
        // verplaats de 6e bit naar de 2e bit en maak de 1e bit 0
        // tel de eerste bit hierbij op
        int externalTwo = ((block >> 4) & ~1) + (block & 1);
        int i = externalTwo;

        // neem de middelste 4 bits en verander naar int
        // verlaats een positie naar rechts en maak de grootste 4 bits 0
        byte middleFour = (byte) ((block >> 1) & ~((8+4+2+1) << 4)) ;
        int j = middleFour;

        return (byte)positions[(i*16) + j];
    }

    private static byte[] joinQuartetsToBytes(byte[] helperBlock) {
        byte[] resultBlock = new byte[helperBlock.length / 2];
        for (int i = 0; i < helperBlock.length; i += 2) {
            resultBlock[i/2] = ByteHelper.joinBlocks(helperBlock[i], helperBlock[i + 1]);
        }
        return resultBlock;
    }
}
