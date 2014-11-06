package cryptogen.des;

import helpers.ByteHelper;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Peter.
 */
public class SyncDesService extends AbstractDesService {

    public void encryptFile(String filePath, byte[][][] subKeys) {
        throw new UnsupportedOperationException();
    }

    public void decryptFile(String filePath, byte[][][] reversedSubKeys) {
        throw new UnsupportedOperationException();
    }

  /*
    private static void encryptFile(String filePath, byte[][] subKeys) {
        System.out.println();
        System.out.println("Encrypting");

        try {
            final File inputFile = new File(filePath);
            final File outputFile = new File(filePath + ".des");
            final InputStream inputStream = new BufferedInputStream(new FileInputStream(inputFile));
            final OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

            final long nbBytesFile = inputFile.length();
            final long nbTotalBlocks = (long) Math.ceil(nbBytesFile / (double) blockSizeInBytes);
            final int nbBytesPaddingNeeded = (int) (blockSizeInBytes - (nbBytesFile % blockSizeInBytes));

            final byte header = (byte) nbBytesPaddingNeeded;
            outputStream.write(header);

            byte[] block = new byte[blockSizeInBytes];
            int bytesRead = 0;

            for (long nbBlocks = 1; nbBlocks <= nbTotalBlocks; nbBlocks++) {

                bytesRead = inputStream.read(block);

                System.out.println("Encrypting block " + nbBlocks);
                byte[] encryptedBlock = encryptBlock(block, subKeys);

                // schrijf geencrypteerd blok weg naar output bestand
                outputStream.write(encryptedBlock);

                block = new byte[blockSizeInBytes];
            }

            inputStream.close();
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void decryptFile(String filePath, byte[][] reversedSubKeys) {
        System.out.println();
        System.out.println("Decrypting");

        try {
            final File inputFile = new File(filePath);
            final File outputFile = new File(filePath.replace(".des",".decrypted"));
            final InputStream inputStream = new BufferedInputStream(new FileInputStream(inputFile));
            final OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

            final long nbBytesFileWithoutHeader = inputFile.length() - 1;
            final long nbTotalBlocks = (long) Math.ceil(nbBytesFileWithoutHeader / (double) blockSizeInBytes);
            final int nbBytesHeading = inputStream.read();

            byte[] block = new byte[blockSizeInBytes];
            for (long nbBlocks = 1; nbBlocks <= nbTotalBlocks; nbBlocks++) {
                inputStream.read(block);

                System.out.println("Decrypting block " + nbBlocks);
                byte[] decryptedBlock = decryptBlock(block, reversedSubKeys);

                // schrijf geencrypteerd blok weg naar output bestand
                // laatste blok => verwijder padding
                if (nbBlocks == nbTotalBlocks) {
                    byte[] blockWithoutPadding = Arrays.copyOfRange(decryptedBlock, 0, blockSizeInBytes - nbBytesHeading);
                    outputStream.write(blockWithoutPadding);
                } else {
                    outputStream.write(decryptedBlock);
                }

                block = new byte[blockSizeInBytes];
            }

            inputStream.close();
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    */

}
