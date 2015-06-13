package cryptogen.des;

import helpers.ConsoleHelper;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Peter.
 */
public class AsyncDesService extends AbstractDesService {

    private static int BLOCK_SIZE_IN_BYTES = DesAlgorithm.blockSizeInBytes;

    public void encryptFile(String filePath, byte[][][] subKeys) {
        System.out.println();
        System.out.println("Encrypting Async");

        try {
            final File inputFile = new File(filePath);
            final File outputFile = new File(filePath + ".des");
            final InputStream inputStream = new BufferedInputStream(new FileInputStream(inputFile));
            final OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

            final List<Future<byte[]>> futures = new ArrayList<>();
            final ExecutorService executor = Executors.newFixedThreadPool(4);

            final long nbBytesFile = inputFile.length();
            final long nbTotalBlocks = (long) Math.ceil(nbBytesFile / (double) BLOCK_SIZE_IN_BYTES);
            final int nbBytesPaddingNeeded = (int) (BLOCK_SIZE_IN_BYTES - (nbBytesFile % BLOCK_SIZE_IN_BYTES));

            final byte header = (byte) nbBytesPaddingNeeded;
            outputStream.write(header);

            long before = System.nanoTime();

            byte[] block = new byte[BLOCK_SIZE_IN_BYTES];
            int bytesRead = 0;
            for (int nbBlocks = 1; nbBlocks <= nbTotalBlocks; nbBlocks++) {

                bytesRead = inputStream.read(block);

                final byte[] finalBlock = block;
                //System.out.println("Encrypting block async " + nbBlocks);
                futures.add(CompletableFuture.supplyAsync(() -> {
                    return DesAlgorithm.encryptBlock(finalBlock, subKeys);
                }, executor).exceptionally(ex -> {
                    throw (RuntimeException) ex;
                }));

                block = new byte[BLOCK_SIZE_IN_BYTES];
            }
            inputStream.close();

            long afterTasks = System.nanoTime();
            ConsoleHelper.append("Done creating futures (" + (afterTasks - before) / 1000000 + " millis)");

            futures.stream().forEachOrdered(encryptedBlock -> {
                try {
                    outputStream.write(encryptedBlock.get());
                    //System.out.println("Blok weggeschreven");
                } catch (InterruptedException | ExecutionException | IOException e) {
                    throw new RuntimeException(e);
                }
            });

            long afterWriting = System.nanoTime();
            
            System.out.println("Done writing to file");
            System.out.println("Setting tasks " + (afterTasks - before)
                + " Writing " + (afterWriting - afterTasks));
            
            ConsoleHelper.appendSuccess("Writing encrypted file to: " + outputFile.getPath()
                + " (" + (afterWriting - afterTasks) / 1000000 + " millis)");

            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void decryptFile(String filePath, byte[][][] reversedSubKeys) {
        System.out.println();
        System.out.println("Decrypting Async");

        try {
            final File inputFile = new File(filePath);
            final File outputFile = new File(filePath.replace(".des",".decryptedasync"));
            final InputStream inputStream = new BufferedInputStream(new FileInputStream(inputFile));
            final OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

            final List<Future<byte[]>> futures = new ArrayList<>();
            final ExecutorService executor = Executors.newFixedThreadPool(4);

            final long nbBytesFileWithoutHeader = inputFile.length() - 1;
            final long nbTotalBlocks = (long) Math.ceil(nbBytesFileWithoutHeader / (double) BLOCK_SIZE_IN_BYTES);
            System.out.println("file length " + inputFile.length()); 
            System.out.println("nbTotalBlocks " + nbTotalBlocks);

            final int nbBytesPadding = inputStream.read();

            byte[] block = new byte[BLOCK_SIZE_IN_BYTES];
            for (int nbBlocks = 1; nbBlocks <= nbTotalBlocks; nbBlocks++) {
                inputStream.read(block);

                final byte[] finalBlock = block;
                //System.out.println("Decrypting block async " + nbBlocks);
                futures.add(CompletableFuture.supplyAsync(() -> {
                    return DesAlgorithm.decryptBlock(finalBlock, reversedSubKeys);
                }, executor).exceptionally(ex -> {
                    throw (RuntimeException) ex;
                }));

                block = new byte[BLOCK_SIZE_IN_BYTES];
            }
            inputStream.close();

            for (int i = 1; i <= nbTotalBlocks; i++) {
                byte[] decryptedBlock = futures.get(i-1).get();
                if  (i == nbTotalBlocks) {
                    decryptedBlock = Arrays.copyOfRange(decryptedBlock, 0, BLOCK_SIZE_IN_BYTES - nbBytesPadding);
                }

                outputStream.write(decryptedBlock);
                //System.out.println("Blok " + i +" weggeschreven");

                // progress to GUI
                final long tenPercent = nbTotalBlocks / 10;
                if (tenPercent != 0 && (i % tenPercent == 0)) {
                    ConsoleHelper.appendPercentCompleted(i, (int) nbTotalBlocks);
                }
            }
            
            System.out.println("Done writing to file");
            ConsoleHelper.appendSuccess("Writing decrypted file to: " + outputFile.getPath());

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
