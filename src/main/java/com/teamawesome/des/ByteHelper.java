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
public class ByteHelper {
    

    // joins two blocks each containing 4 significant bits (4 right most bits contain data)
    public static byte joinBlocks(byte block1, byte block2) {
        return (byte) (block1 << 4 | block2);
    }
    
    // Herschikt de bits in de source byte array volgens de positions array
    // - Lengte van resultaat wordt bepaald door lengte van positions array
    public static byte[] permutate(byte[] source, int[] positions) {

        byte[] newBlock = new byte[positions.length / 8];
        int byteIndex = -1;

        // voor elke positie
        for (int i = 0; i < positions.length; i++) {
            // neem de index van de bit
            int bitIndex = positions[i] - 1;
            // neem de waarde van de bit (0 of 1)
            byte bit = getBit(source, bitIndex);

            if (i % 8 == 0)
                byteIndex++;
            
            // zet de bit in de nieuwe blok op de juiste plaats
            newBlock[byteIndex] = (byte) ((bit << (bitIndex % 8)) | newBlock[byteIndex]);
        }

        return newBlock;
    }

    // Kijkt of een bepaalde bit in een byte array gelijk aan 1 is
    public static byte getBit(byte[] data, int pos) {
        int posByte = pos / 8;
        int posBit = pos % 8;
        byte valByte = data[posByte];

        if (isBitSet(valByte, posBit)) {
            return 1;
        } else {
            return 0;
        }
    }

    // Kijkt of een bepaalde bit in een byte gelijk aan 1 is
    // Source: http://stackoverflow.com/questions/1034473/java-iterate-bits-in-byte-array
    public static Boolean isBitSet(byte value, int bit) {
        return (value & (1 << bit)) != 0;
    }

    // Voert XOR uit op twee byte arrays
    // Arrays moeten van de zelfde lengte zijn
    public static byte[] xorByteBlocks(byte[] blockOne, byte[] blockTwo) {
        byte[] newBlock = new byte[blockOne.length];

        for (int i = 0; i < newBlock.length; i++) 
            newBlock[i] = (byte) (blockOne[i] ^ blockTwo[i]);
        
        return newBlock;
    }
}
