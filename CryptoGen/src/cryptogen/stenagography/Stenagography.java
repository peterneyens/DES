/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cryptogen.stenagography;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JTextArea;

/**
 *
 * @author Cornel
 */
public class Stenagography {

    public static JTextArea console;
    public static boolean DEBUG = false;

    /*
     gebaseerd op http://www.dreamincode.net/forums/topic/27950-steganography/
     */
    public static BufferedImage encode(String path, String msg) {

        //foto ophalen van disk
        BufferedImage selectedImage = getImage(path);

        //kopie maken van geselecteerde foto zodat de foto aangepast kan worden in Java (userspace)
        BufferedImage img = cloneImage(selectedImage);

        //foto omzetten naar een byte array
        byte bImg[] = getBytes(img);
        
        /*for (int i = 0; i < bImg.length; i++) {
            System.out.println(Integer.toBinaryString(bImg[i]));
        }*/

        //tekst omzetten naar een byte array
        byte bMsg[] = msg.getBytes();

        //byte array maken van de lengte van de tekst
        byte bLen[] = getBytes(bMsg.length);

        //lengte van de tekst in de eerste 32 bits van de foto zetten
        bImg = addText(bImg, bLen, 0);

        //tekst in de foto zetten
        bImg = addText(bImg, bMsg, 32); //32 offset voor de lengte

        //byte array omzetten naar foto
        img = getImage(bImg);

        console.append("Encoding has been completed!" + "\n\r");

        return img;
    }

    public static String decode(BufferedImage img) {
        //foto omzetten naar een byte array van rgb waardes (r, g, b, r, g, b, ...)
        byte bImg[] = getBytes(img);

        String msg = "";

        int max = (int) bImg.length; //aantal letters dat max in img passen

        //loop door elke letter = 8 bytes
        for (int i = 0; i < max; i++) {
            Byte ascii = 0;

            //loop door elke byte van de 8 bytes
            for (int j = 0; j < 8; j++) {
                //pak de desbetreffende byte van de afbeelding en voeg deze toe aan de assci byte om zo na 8 loops een ascii waarde te krijgen  
                if ((bImg[i * 8 + j] >> j) == 1) // de jde bit opvragen dmv een right shift
                {
                    ascii = (byte) (bImg[i * 8 + j] | (1 << j));
                } else {
                    ascii = (byte) (bImg[i * 8 + j] & ~(1 << j));
                }
            }

            //asci is nu compleet --> omzetten naar tekst en toevoegen aan uitkomst
            msg = msg + ascii.toString();
        }

        return msg;
    }

    /*
     Ophalen van een foto aan de hand van het pad
     */
    public static BufferedImage getImage(String path) {
        File f = new File(path);
        BufferedImage img = null;

        try {
            //covnerteer the bestand naar een foto
            img = ImageIO.read(f);
        } catch (Exception ex) {
            if (console != null) {
                console.append("Fatal Error: " + ex.getMessage() + "\n\r");
            }
        }
        return img;
    }

    /*
     Kopieerd de ingegeven foto en geeft ene kopie hiervan terug
     */
    public static BufferedImage cloneImage(BufferedImage selectedImage) {
        BufferedImage image = new BufferedImage(selectedImage.getWidth(), selectedImage.getHeight(), selectedImage.getType());
        Graphics g = image.getGraphics();
        g.drawImage(selectedImage, 0, 0, null);
        g.dispose();

        return image;
    }

    /*
     Waarde encoderen in de foto met ene bepaalde offset
    
     Door elke byte van de waarde loopen en deze bit per bit toekennen aan de opeenvolgende laatste bits van de bytes van de foto
    
     vb:
     byte van de val: 87
    
     bit 7:
     01010111 = 87
     >>> right shift van 7
     00000000 = 0
     00000001 --> om de bit te verkrijgen
     -------- &
     00000000
    
     bit 6:
     01010111 = 87
     >>> right shift van 6
     00000001 = 1
     00000001 --> om de bit te verkrijgen
     -------- &
     00000001
    
     bit 5:
     01010111 = 87
     >>> right shift van 5
     00000010 = 2
     00000001 --> om de bit te verkrijgen
     -------- &
     00000000
    
     bit 4:
     01010111 = 87
     >>> right shift van 4
     00000101 = 5
     00000001 --> om de bit te verkrijgen
     -------- &
     00000001
    
     bit 3:
     01010111 = 87
     >>> right shift van 3
     00001010 = 10
     00000001 --> om de bit te verkrijgen
     -------- &
     00000000
    
     bit 2:
     01010111 = 87
     >>> right shift van 2
     00010101 = 21
     00000001 --> om de bit te verkrijgen
     -------- &
     00000001
    
     bit 1:
     01010111 = 87
     >>> right shift van 1
     00101011 = 43
     00000001 --> om de bit te verkrijgen
     -------- &
     00000001
    
     bit 0:
     01010111 = 87
     >>> right shift van 0
     01010111 = 87
     00000001 --> om de bit te verkrijgen
     -------- &
     00000001
     
     --> we hebben de waarde van de bit, nu de bit nog in de igage byte stoppen:
    
     We doen een OR operator op 0xFE en de gevonden bit om zo de laatste bit op 0 of 1 te zetten
    
     1111 1110
     0000 0000
     --------- |
     1111 1110 -->veranderd naar 0
    
     1111 1110
     0000 0001
     --------- |
     1111 1111 -->veranderd naar 1
    
     vb:
    
     1010 1101 = originele waarde
     1111 1110 = 0xFE laatste bit op 0 zetten
     --------- &
     1010 1100
    
     1010 1101 = originele waarde
     1111 1111 = 0xFE laatste bit op 1 zetten
     --------- &
     1010 1101
    
     */
    public static byte[] addText(byte[] bImg, byte[] bVal, int offset) {
        try {
            //check of de tekst niet te groot is voor de foto
            if (bVal.length + 32 > bImg.length) {
                throw new IllegalArgumentException("Message is too big for this image!");
            }

            //loop door alle bytes van de meegegeven waarde (val)
            for (int i = 0; i < bVal.length; i++) {
                if (DEBUG) {
                    console.append("Encoding of byte " + i + " started. \n\r");
                }

                //loop door de 8 bits vat de i byte
                for (int bit = 7; bit >= 0; --bit, ++offset) { //increment offset na elke iteratie
                    //desbetreffende bit uit tekst byte ophalen
                    int b = (bVal[i] >>> bit) & 1;

                    //bit in de afbeelding veranderen
                    bImg[offset] = (byte) ((bImg[offset] & 0xFE) | b);

                    if (DEBUG) {
                        console.append("\t" + "value byte " + i + ", " + "bit " + bit + ": Encoded into image byte " + offset + " with value " + b + "\n\r");
                    }

                }
            }
        } catch (Exception ex) {
            if (console != null) {
                console.append("Fatal Error: " + ex.getMessage() + "\n\r");
            }
        }

        return bImg;
    }

    /*
     Een foto omzetten naar een byte array
     */
    public static byte[] getBytes(BufferedImage img) {
        byte[] bImg = null;

        try {
            ByteArrayOutputStream bStream = new ByteArrayOutputStream();
            ImageIO.write(img, "bmp", bStream);
            bImg = bStream.toByteArray();
        } catch (Exception ex) {
            if (console != null) {
                console.append("Fatal Error: " + ex.getMessage() + "\n\r");
            }
        }

        return bImg;
    }

    /*
     Een nummer omzetten naar een byte[4] array (32 bits)
     */
    public static byte[] getBytes(int val) {
        //uitleg:
        //Voor de lengte van de tekst houden we 4 bytes in het begin van de afbeelding vrij.
        //vb: val = 287, 
        // 
        // 3de byte: 24 ste tot de 31ste bit
        // 2de byte: 16 ste tot de 23ste bit
        // 1de byte: 8 ste tot de 15ste bit
        // 0de byte: 0 ste tot de 7ste bit
        //
        //berekening 0de byte
        //00000000 00000000 00000001 00011111 = 287
        //11111111 00000000 00000000 00000000 = 4278190080 or 0xFF000000
        //------------------------------------ AND
        //00000000 00000000 00000000 00000000 = 31
        //
        //berekening 1de byte
        //00000000 00000000 00000001 00011111 = 287
        //00000000 11111111 00000000 00000000 = 16711680 or 0x00FF0000
        //------------------------------------ AND
        //00000000 00000000 00000000 00000000 = 31
        //
        //berekening 2de byte
        //00000000 00000000 00000001 00011111 = 287
        //00000000 00000000 11111111 00000000 = 65280 or 0x0000FF00
        //------------------------------------ AND
        //00000000 00000000 00000001 00000000 = 256
        //
        //berekening 3de byte
        //00000000 00000000 00000001 00011111 = 287
        //00000000 00000000 00000000 11111111 = 255 or 0x000000FF
        //------------------------------------ AND
        //00000000 00000000 00000000 00011111 = 31

        //de waarde van de byte ophalen en naar rechts verschuiven om naar een byte te converteren
        byte b3 = (byte) ((val & 0xFF000000) >>> 24);
        byte b2 = (byte) ((val & 0x00FF0000) >>> 16);
        byte b1 = (byte) ((val & 0x0000FF00) >>> 8);
        byte b0 = (byte) ((val & 0x000000FF));

        return (new byte[]{b3, b2, b1, b0});

    }

    /*
     Een foto byte array omzetten naar een foto
     */
    public static BufferedImage getImage(byte[] arr) {
        
        /*for (int i = 0; i < arr.length; i++) {
            System.out.println(Integer.toBinaryString(arr[i]));
        }*/
        
        BufferedImage img = null;

        try {
            img = ImageIO.read(new ByteArrayInputStream(arr));
        } catch (Exception ex) {
            if (console != null) {
                console.append("Fatal Error: " + ex.getMessage() + "\n\r");
            }
        }
        return img;
    }
}
