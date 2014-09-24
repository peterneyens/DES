/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cryptogen;

import helpers.ByteArrayBitIterable;
import helpers.ByteHelper;

/**
 *
 * @author arno
 */
public class Feistel {
    
    public static int[] expansionTabel42Bits = new int[]{32, 1, 2, 3, 4, 5, 4, 5, 6, 7, 8, 9, 8, 9, 10, 11, 12, 13, 12, 13, 14, 15, 16, 17, 16, 17, 18, 19, 20, 21, 20, 21, 22, 23, 24, 25, 24, 25, 26, 27, 28, 29, 28, 29, 30, 31, 32, 1};

    // gebruikt door S functie
    public static int[][] sMatrix = new int[][]{
        {14, 4, 13, 1, 2, 15, 11, 8, 3, 10, 6, 12, 5, 9, 0, 7},
        {0, 15, 7, 4, 14, 2, 13, 1, 10, 6, 12, 11, 9, 5, 3, 8},
        {4, 1, 14, 8, 13, 6, 2, 11, 15, 12, 9, 7, 3, 10, 5, 0},
        {15, 12, 8, 2, 4, 9, 1, 7, 5, 11, 3, 14, 10, 0, 6, 13}
    };
    
    public static int[] permutatieTabel32Bits = new int[]{16, 7, 20, 21, 29, 12, 28, 17, 1, 15, 23, 26, 5, 18, 31, 10, 2, 8, 24, 14, 32, 27, 3, 9, 19, 13, 30, 6, 22, 11, 4, 25};
    
    /* 
    
    Executes cipher function
    
    INPUT
    block -  32bits - 4bytes
    subkey - 48bits - 6bytes
    
    */
    public byte[] executeFunction(byte[] block, byte[] subkey) throws Exception {
        
        if (block.length != 4)
            throw new Exception("Block should be 4 blocks long.");
        else if (subkey.length != 6)
            throw new Exception("Key should be 6 blocks long.");
        

        byte[] transmutedBlock = ByteHelper.permutate(block, Feistel.expansionTabel42Bits);
        byte[] xoredBytes = ByteHelper.xorByteBlocks(transmutedBlock, subkey);
        byte[] result = executeS(xoredBytes);
        return ByteHelper.permutate(result, Feistel.permutatieTabel32Bits);
    }

    private byte[] executeS(byte[] block) {

        byte[] helperBlock = new byte[8];
        byte[] resultBlock = new byte[4];

        int count = 0,
            byteIndex = 0;

        ByteArrayBitIterable bitStream = new ByteArrayBitIterable(block);

        // itterate over each bit
        for (boolean isBitSet : bitStream) {
            // get bit value
            byte bit = (isBitSet) ? (byte) 1 : (byte) 0;

            if (count % 6 == 0 && count != 0) // count starts at 0, so byteIndex was incremented to 1 directly
                byteIndex++;

            helperBlock[byteIndex] = (byte) ((helperBlock[byteIndex] << 1) | bit);
            count++;
        }

        // execute S function on each byte
        // each resulting byte contains 4 bits
        for (int i = 0; i < helperBlock.length; i++) {
            helperBlock[i] = S(helperBlock[i], Feistel.sMatrix);
        }

        // join blocks
        for (int i = 0; i < helperBlock.length; i += 2) {
            //resultBlock[i] = ByteHelper.joinBlocks(helperBlock[i], helperBlock[i + 1]);
            resultBlock[i/2] = ByteHelper.joinBlocks(helperBlock[i], helperBlock[i + 1]);

        }

        return resultBlock;
    }

    /* Implementeert S functie
    
    INPUT
    block        -- byte met 6 bits
    matrix       -- matrix van mogelijke getallen
    
    */
    private byte S(byte block, int[][] matrix) {
        
        // neem eesrste en zesde bit en verander naar int
        int i = Byte.parseByte(
                (ByteHelper.isBitSet(block, 0) ? "1" : "0") +
                (ByteHelper.isBitSet(block, 5) ? "1" : "0") , 2);
        
        // neem midenste 4 bits en verander naar int
        int j = Byte.parseByte(
                (ByteHelper.isBitSet(block, 1) ? "1" : "0") +
                (ByteHelper.isBitSet(block, 2) ? "1" : "0") +
                (ByteHelper.isBitSet(block, 3) ? "1" : "0") +
                (ByteHelper.isBitSet(block, 4) ? "1" : "0") , 2);
        
        return (byte)matrix[i][j];
        
    }

}
