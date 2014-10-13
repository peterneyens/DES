/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cryptogen.stenagography;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
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
     *Encrypt an image with text, the output file will be of type .png
     *@param path           Het pad naar de bmp foto
     *@param msg            het bericht dat geencodeerd moet worden
    
     gebaseerd op http://www.dreamincode.net/forums/topic/27950-steganography/
     */
    public static BufferedImage encode(String path, String msg, boolean file) {

        //foto ophalen van disk
        BufferedImage selectedImage = getImage(path);

        //kopie maken van geselecteerde foto zodat de foto aangepast kan worden in Java (userspace)
        BufferedImage img = cloneImage(selectedImage);
        if (file) {
            img = encodeMessage(img, msg, file);
        } else {
            img = encodeMessage(img, msg, file);
        }

        console.append("Encoding has been completed!" + "\n\r");

        return (img);
    }

    public static BufferedImage encodeMessage(BufferedImage img, String msg, boolean file) {
        //foto omzetten naar een byte array
        byte bImg[] = getBytes(img);
        //Controle of er gelezen wordt van een DES file
        if (file) {
            // byte gegevensopslaan en uitlezen
            byte[] block = null;
            int bLen = 0;
            try {
                final File inputFile = new File(msg);
                final InputStream inputStream = new BufferedInputStream(new FileInputStream(inputFile));

                final long nbBytesFile = inputFile.length();

                if (nbBytesFile > (long) Integer.MAX_VALUE) {
                    throw new Exception("File to big.");
                } else {
                    bLen = (int) nbBytesFile;
                    block = new byte[bLen];
                }
                inputStream.read(block);
                System.out.println("Reading file with: " + nbBytesFile + " bytes");

                inputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            addText(bImg, getBytes(bLen), 0);
            addText(bImg, block, 32);
        } else {
            //tekst omzetten naar een byte array
            byte bMsg[] = msg.getBytes();

            //byte array maken van de lengte van de tekst
            byte bLen[] = getBytes(bMsg.length);

            //lengte van de tekst in de eerste 32 bits van de foto zetten
            addText(bImg, bLen, 0);

            //tekst in de foto zetten
            addText(bImg, bMsg, 32); //32 offset voor de lengte
        }
        //compare(getBytes(selectedImage), bImg, bMsg.length * 8 + 32);
        //byte array omzetten naar foto
        //img =  getImage(bImg);
        return img;
    }

    public static String decode(String path, boolean file) {
        //foto ophalen van disk
        BufferedImage selectedImage = getImage(path);

        //kopie maken van geselecteerde foto zodat de foto aangepast kan worden in Java (userspace)
        BufferedImage img = cloneImage(selectedImage);

        //retrieve message from image
        String msg = decodetext(img);
        
        //Scrijft message naar een file EN output box
        if (file) {
            try {
                final File outputFile = new File(path + ".des2");
                final OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));
                outputStream.write(msg.getBytes());
                outputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return msg;
    }

    public static String decodetext(BufferedImage img) {
        //convert image to byte array
        byte[] bImg = getBytes(img);

        //offset definieren voor later
        int offset = 32;

        //retrieve lengths from the first bytes. 0-> 31 bits
        int length = 0;

        for (int i = 0; i < offset; i++) {
            //de lengte byte doorschuiven naar links en de laatste but van de image byte op de ereste plaats zetten dmv OR
            //
            //0000 0000 0000 0000 0000 0000 0000 0000
            //0000 0000 0000 0000 0000 0000 0000 0000 = doorschuiven naar links
            //
            //                             [0001 0011 = image byte i]
            //                             [0000 0001               ]
            //                              --------- & = AND
            //                              0000 0001 = laatste bit
            //
            //--------------------------------------- | = OR tussen doogeschoven lengte en laatste bit
            //0000 0000 0000 0000 0000 0000 0000 0001
            length = (length << 1) | bImg[i] & 1;
        }

        //byte array aanmaken voor de tekst nu we de lengte weten
        byte[] bMsg = new byte[length];

        //elke byte van de tekst loopen om de laatste bits  op de halen van de tekst
        for (int i = 0; i < bMsg.length; i++) {
            //8 keer de laatste bit ophalen van de byte van de foto om zo de tekst byte o pte bouwen
            for (int bit = 0; bit < 8; ++bit, ++offset) {
                //zelfde principe als de lengte
                bMsg[i] = (byte) ((bMsg[i] << 1) | (bImg[offset] & 1));
            }
        }

        //converteer message byte array naar string
        return new String(bMsg);
    }

    /*
     Ophalen van een foto aan de hand van het pad
     */
    public static BufferedImage getImage(String path) {
        File f = new File(path);
        BufferedImage img = null;

        try {
            //converteer the bestand naar een foto
            img = ImageIO.read(f);
        } catch (Exception ex) {
            if (console != null) {
                console.append("Fatal Error (getImage(String)): " + ex.getMessage() + "\n\r");
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
            if (bVal.length + offset > bImg.length) {
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
                console.append("Fatal Error addText(byte[], byte[], int): " + ex.getMessage() + "\n\r");
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
            WritableRaster raster = img.getRaster();
            DataBufferByte buffer = (DataBufferByte) raster.getDataBuffer();

            return buffer.getData();
        } catch (Exception ex) {
            if (console != null) {
                console.append("Fatal Error (getBytes(BufferedImage)): " + ex.getMessage() + "\n\r");
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
        //System.out.println(getType(arr));
        BufferedImage img = null;

        /*String[] names = ImageIO.getWriterFormatNames();
         for (String name : names) {
         System.out.println(name);
         }*/
        try {
            InputStream in = new ByteArrayInputStream(arr);
            img = ImageIO.read(in);
        } catch (Exception ex) {
            if (console != null) {
                console.append("Fatal Error (getImage(byte[])): " + ex.getMessage() + "\n\r");
            }
        }
        if (img == null) {
            throw new IllegalArgumentException("Can not convert byte array to image!");
        }

        return img;
    }

    //comparing 2 byte arrays for debugging
    public static void compare(byte[] val1, byte[] val2, int len) {
        if (val1.length != val2.length) {
            System.out.println("2 arrays are not the same length!");
        } else {
            for (int i = 0; i < len; i++) {
                System.out.print("Index " + i + ": " + Integer.toBinaryString(val1[i]) + " - " + Integer.toBinaryString(val2[i]));

                if (val1[i] != val2[i]) {
                    System.out.println("!!!");
                } else {
                    System.out.println();
                }
            }
        }
    }
}
