/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package helpers;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import static java.lang.System.out;

/**
 *
 * @author arno
 */
public class ByteHelper {
    
    public static byte[] concatenateBits(byte[] a, int aLen, byte[] b, int bLen) {
        int numOfBytes = (aLen + bLen - 1) / 8 + 1;
        byte[] out = new byte[numOfBytes];
        int j = 0;
        for (int i = 0; i < aLen; i++) {
            int val = ByteHelper.getBit(a, i);
            ByteHelper.setBit(out, j, val);
            j++;
        }
        for (int i = 0; i < bLen; i++) {
            int val = ByteHelper.getBit(b, i);
            ByteHelper.setBit(out, j, val);
            j++;
        }
        return out;
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

    public static byte[] fileToBytes(String file) throws Exception {
        FileInputStream inputStream = new FileInputStream(file);
        int size = inputStream.available();

        byte[] buffer = new byte[size];
        inputStream.read(buffer);
        inputStream.close();

        return buffer;
    }

    public static void bytesTofile(byte[] data, String file) throws Exception {
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(data);
        fos.close();
    }

    public static void printBytesAsBits(byte[] bytes) {
        for (byte b : bytes) {
            System.out.println(Integer.toBinaryString(b & 255 | 256).substring(1));
        }
        System.out.println();
    }

    // joins two blocks each containing 4 significant bits (4 right most bits contain data)
    public static byte joinBlocks(byte block1, byte block2) {
        return (byte) (block1 << 4 | block2);
    }

    // Herschikt de bits in de 'in' byte array volgens de 'map' array
    // Lengte van resultaat wordt bepaald door lengte van 'map' array
    public static byte[] permutate(byte[] in, int[] map) {
        int numOfBytes = (map.length - 1) / 8 + 1;
        byte[] out = new byte[numOfBytes];
        for (int i = 0; i < map.length; i++) {
            int val = getBit(in, map[i] - 1);
            setBit(out, i, val);
        }
        return out;
    }

    // Kijkt of een bepaalde bit in een byte array gelijk aan 1 is
    public static int getBit(byte[] data, int pos) {
        int posByte = pos / 8;
        int posBit = pos % 8;
        byte valByte = data[posByte];
        int valInt = valByte >> (8 - (posBit + 1)) & 0x0001;
        return valInt;
    }

    // Kijkt of een bepaalde bit in een byte gelijk aan 1 is
    // Source: http://stackoverflow.com/questions/1034473/java-iterate-bits-in-byte-array
    public static Boolean isBitSet(byte value, int bit) {
        return (value & (1 << bit)) != 0;
    }

    // Voert XOR uit op twee byte arrays
    // Arrays moeten van de zelfde lengte zijn
    public static byte[] xorByteBlocks(byte[] blockOne, byte[] blockTwo) {

        byte[] out = new byte[blockOne.length];
        for (int i = 0; i < blockOne.length; i++) {
            out[i] = (byte) (blockOne[i] ^ blockTwo[i]);
        }
        return out;
//        byte[] newBlock = new byte[blockOne.length];
//
//        for (int i = 0; i < newBlock.length; i++) {
//            newBlock[i] = (byte) (blockOne[i] ^ blockTwo[i]);
//        }
//
//        return newBlock;
    }

    // Zorgt voor de left shift
    // Source: http://www.herongyang.com/Java/Bit-String-Left-Rotation-All-Bits-in-Byte-Array.html
    public static byte[] rotateLeft(byte[] in, int len, int step) {
        int numOfBytes = (len - 1) / 8 + 1;
        byte[] out = new byte[numOfBytes];
        for (int i = 0; i < len; i++) {
            int val = getBit(in, (i + step) % len);
            setBit(out, i, val);
        }
        return out;
    }

    /*
     //nick: Arno heeft hier een functie voor weet niet zeker of zelfde resultaat, later testen.
     // Haalt de bit op op positie pos in de byte array data
     // Source: http://www.herongyang.com/Java/Bit-String-Get-Bit-from-Byte-Array.html
     private static int getBit(byte[] data, int pos) {
     int posByte = pos / 8;
     int posBit = pos % 8;
     byte valByte = data[posByte];
     int valInt = valByte >> (8 - (posBit + 1)) & 0x0001;
     return valInt;
     }
     */
    //nick: misschien deze functie zelf uitschrijven, deze komt rechstreeks van de site.
    // Stelt de bit op op positie pos in de byte array data
    // Source: http://www.herongyang.com/Java/Bit-String-Get-Bit-from-Byte-Array.html
    public static void setBit(byte[] data, int pos, int val) {
        int posByte = pos / 8;
        int posBit = pos % 8;
        byte oldByte = data[posByte];
        oldByte = (byte) (((0xFF7F >> posBit) & oldByte) & 0x00FF);
        byte newByte = (byte) ((val << (8 - (posBit + 1))) | oldByte);
        data[posByte] = newByte;
    }

}
