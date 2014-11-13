package cryptogen.des;

import helpers.ConsoleHelper;
import java.io.*;
import java.util.Arrays;

/**
 * @author Peter.
 */
public class SyncDesService extends AbstractDesService {

    private static final int BLOCK_SIZE_IN_BYTES = DesAlgorithm.blockSizeInBytes;

    public void encryptFile(String filePath, byte[][][] subKeys) {
        System.out.println();
        System.out.println("Encrypting");

        try {
            final File inputFile = new File(filePath);
            final File outputFile = new File(filePath + ".des");
            final InputStream inputStream = new BufferedInputStream(new FileInputStream(inputFile));
            final OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

            final long nbBytesFile = inputFile.length();
            final long nbTotalBlocks = (long) Math.ceil(nbBytesFile / (double) BLOCK_SIZE_IN_BYTES);
            final int nbBytesPaddingNeeded = (int) (BLOCK_SIZE_IN_BYTES - (nbBytesFile % BLOCK_SIZE_IN_BYTES));

            final byte header = (byte) nbBytesPaddingNeeded;
            outputStream.write(header);

            byte[] block = new byte[BLOCK_SIZE_IN_BYTES];
            int bytesRead = 0;

            for (long nbBlocks = 1; nbBlocks <= nbTotalBlocks; nbBlocks++) {

                bytesRead = inputStream.read(block);

                //System.out.println("Encrypting block " + nbBlocks);
                byte[] encryptedBlock = DesAlgorithm.encryptBlock(block, subKeys);

                // schrijf geencrypteerd blok weg naar output bestand
                outputStream.write(encryptedBlock);

                // progress to GUI
                final long tenPercent = nbTotalBlocks / 10;
                if (tenPercent != 0 && nbBlocks % tenPercent == 0) {
                    ConsoleHelper.appendPercentCompleted((int) nbBlocks, (int) nbTotalBlocks);
                }

                block = new byte[BLOCK_SIZE_IN_BYTES];
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

    public void decryptFile(String filePath, byte[][][] reversedSubKeys) {
        System.out.println();
        System.out.println("Decrypting");

        try {
            final File inputFile = new File(filePath);
            final File outputFile = new File(filePath.replace(".des",".decrypted"));
            final InputStream inputStream = new BufferedInputStream(new FileInputStream(inputFile));
            final OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

            final long nbBytesFileWithoutHeader = inputFile.length() - 1;
            final long nbTotalBlocks = (long) Math.ceil(nbBytesFileWithoutHeader / (double) BLOCK_SIZE_IN_BYTES);
            final int nbBytesHeading = inputStream.read();

            byte[] block = new byte[BLOCK_SIZE_IN_BYTES];
            for (long nbBlocks = 1; nbBlocks <= nbTotalBlocks; nbBlocks++) {
                inputStream.read(block);

                //System.out.println("Decrypting block " + nbBlocks);
                byte[] decryptedBlock = DesAlgorithm.decryptBlock(block, reversedSubKeys);

                // schrijf geencrypteerd blok weg naar output bestand
                // laatste blok => verwijder padding
                if (nbBlocks == nbTotalBlocks) {
                    byte[] blockWithoutPadding = Arrays.copyOfRange(decryptedBlock, 0, BLOCK_SIZE_IN_BYTES - nbBytesHeading);
                    outputStream.write(blockWithoutPadding);
                } else {
                    outputStream.write(decryptedBlock);
                }
                
                // progress to GUI
                final long tenPercent = nbTotalBlocks / 10;
                if (tenPercent != 0 && nbBlocks % tenPercent == 0) {
                    ConsoleHelper.appendPercentCompleted((int) nbBlocks, (int) nbTotalBlocks);
                }

                block = new byte[BLOCK_SIZE_IN_BYTES];
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

}
