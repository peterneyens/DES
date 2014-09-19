/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.teamawesome.des;

/**
 *
 * @author arno
 */
public class feistel {
   
    public static int [] selectionTable = new int []{32, 1, 2, 3, 4, 5, 4, 5, 6, 7, 8, 9, 8, 9, 10, 11, 12, 13, 12, 13, 14, 15, 16, 17, 16, 17, 18, 19, 20, 21, 20, 21, 22, 23, 24, 25, 24, 25, 26, 27, 28, 29,28, 29, 30, 31, 32, 1};
    public static int [][] selectionTable2 = new int [][] {{14, 4, 13, 1, 2, 15, 11, 8, 3, 10, 6, 12, 5, 9, 0, 7},
                                                            {0, 15, 7, 4, 14, 2, 13, 1, 10, 6, 12, 11, 9, 5, 3, 8},
                                                            {4, 1, 14, 8, 13, 6, 2, 11, 15, 12, 9, 7, 3, 10, 5, 0},
                                                            {15, 12, 8, 2, 4, 9, 1, 7, 5, 11, 3, 14, 10, 0, 6, 13}};
    public static int [] selectionTable3 = new int[]{16, 7, 20, 21, 29, 12, 28, 17, 1, 15, 23, 26, 5, 18, 31, 10, 2, 8, 24, 14, 32, 27, 3, 9, 19, 13, 30, 6, 22, 11, 4, 25};
    
    // expands 32 bit block to 48 bit
    public static byte [] get(byte [] block, byte[] subkey) throws Exception {
        
        // block -  32bits - 4bytes
        // subkey - 48bits - 6bytes
        if (block.length != 4)
            throw new Exception("Block should be 4 blocks long.");
        else if(subkey.length != 6)
            throw new Exception("Key should be 6 blocks long.");
        
        
        byte [] originalArrayOfBits = new byte[32];
        int index = 0;
        
        for (int i = 0; i < 32; i++) {
            originalArrayOfBits[i] = getBit(block, i);
        }
        
        byte [] expandedArrayOfBits = expandByteArray(originalArrayOfBits, feistel.selectionTable);
        byte [] expandedArrayOfBytes = reduceBytesToBits(expandedArrayOfBits);
        
        byte [] xoredArrayOfBytes = xorByteBlocks(expandedArrayOfBytes, subkey);
        
        
        return xoredArrayOfBytes;
    }

    // http://www.herongyang.com/Java/Bit-String-Get-Bit-from-Byte-Array.html
    private static byte getBit(byte[] data, int pos) {
      int posByte = pos/8; 
      int posBit = pos%8;
      byte valByte = data[posByte];
      int valInt = valByte>>(8-(posBit+1)) & 0x0001;
      return (byte)valInt;
   }
    
   private static byte[] expandByteArray(byte [] block, int [] positionTabel){
       int newBlockSize = positionTabel.length;
       byte [] newBlock = new byte[newBlockSize];
       
       for (int i = 0; i < newBlockSize; i++) {
           newBlock[i] = block[positionTabel[i]];
       }
       
       return newBlock;
   }
   
   // each byte in array has data in first bit
   private static byte[] reduceBytesToBits(byte [] block) {
       byte [] newBlock = new byte [6];
       byte helper = 0;
       
       
       for (int i = 0; i < block.length; i++) {
            helper = (byte)((helper << 1) & block[i]);
            
            if(i%8 == 0) {
                newBlock[i/8] = helper;
                helper = 0;
            }
       }
       
       return newBlock;
   }
    
   
       
    private static byte[] xorByteBlocks(byte [] blockOne, byte [] blockTwo){
        byte [] newBlock = new byte[blockOne.length];
        
        for (int i = 0; i < newBlock.length; i++) {
            newBlock[i] = (byte)(blockOne[i] ^ blockTwo[i]);
        }
        
        return newBlock;
    }
}
