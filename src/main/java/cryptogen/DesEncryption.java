package cryptogen;

import helpers.ByteHelper;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

/**
 * Created by peter on 22/09/14.
 */
public class DesEncryption {

    private static final int blockSizeInBytes = 8;

    private static final int[] initialPermutation = new int[] {
        58, 50, 42, 34, 26, 18, 10, 2,
        60, 52, 44, 36, 28, 20, 12, 4,
        62, 54, 46, 38, 30, 22, 14, 6,
        64, 56, 48, 40, 32, 24, 16, 8,
        57, 49, 41, 33, 25, 17, 9, 1,
        59, 51, 43, 35, 27, 19, 11, 3,
        61, 53, 45, 37, 29, 21, 13, 5,
        63, 55, 47, 39, 31, 23, 15, 7
    };

    private static final int[] inverseInitialPermutation = new int[] {
        40, 8, 48, 16, 56, 24, 64, 32,
        39, 7, 47, 15, 55, 23, 63, 31,
        38, 6, 46, 14, 54, 22, 62, 30,
        37, 5, 25, 13, 53, 21, 61, 29,
        36, 4, 24, 12, 52, 20, 60, 28,
        35, 3, 23, 11, 51, 19, 59, 27,
        34, 2, 42, 10, 50, 18, 58, 26,
        33, 1, 41, 9, 49, 17, 57, 25
    };

    public static void encryptFile(String filePath, String key) {
        // eerste 64 bits van key omgezet in bytes
        byte[] keyInBytes = Arrays.copyOfRange(key.getBytes(Charset.forName("UTF-8")), 0, 8);
        System.out.println("key in bytes : " + keyInBytes.length);

        // TODO key checking
        // 8th bit now not used, -> pariteitsbit (bytes hebben oneven aantal 1)
        // TODO check keyInBytes niet 00000000 of 111111111, of subkeys vaak gelijk, ...

        long before = System.nanoTime();
        encryptFile(filePath, keyInBytes);
        long afterSync = System.nanoTime();
        encryptFileAsync(filePath, keyInBytes);
        long afterAsync = System.nanoTime();

        System.out.println("Sync " + (afterSync - before) + " Async " + (afterAsync - afterSync));
    }

    private static void encryptFile(String filePath, byte[] keyInBytes) {
        byte[][] subkeys = new KeyCalculator().Generate(keyInBytes);
        System.out.println("Subkeys gegenereerd");

        //InputStream inputStream = null;
        //OutputStream outputStream = null;

        try {
            File inputFile = new File(filePath);
            File outputFile = new File(filePath + ".des");
            if(! outputFile.exists()) {
                outputFile.createNewFile();
            }

            InputStream inputStream = new BufferedInputStream(new FileInputStream(inputFile));
            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

            int nbBlocks = 0;
            byte[] block = new byte[blockSizeInBytes];
            int bytesRead = 0;
            while ((bytesRead = inputStream.read(block)) >= 0) {

                if (bytesRead != blockSizeInBytes) {
                    System.out.println("Did not read a full block (8 bytes != " + bytesRead + " bytes)");
                    System.out.println("Bytes in block: " + block.length);
                    // TODO ??
                }

                nbBlocks++;
                System.out.println("Encrypting block " + nbBlocks);
                byte[] encryptedBlock = encryptBlock(block, subkeys);

                // schrijf geencrypteerd blok weg naar output bestand
                outputStream.write(encryptedBlock);
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

    private static void encryptFileAsync(String filePath, byte[] keyInBytes) {

        byte[][] subkeys = new KeyCalculator().Generate(keyInBytes);
        System.out.println("Subkeys gegenereerd");

        //InputStream inputStream = null;
        //OutputStream outputStream = null;

        try {
            File inputFile = new File(filePath);
            File outputFile = new File(filePath + ".des2");
            if (!outputFile.exists()) {
                outputFile.createNewFile();
            }

            InputStream inputStream = new BufferedInputStream(new FileInputStream(inputFile));
            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

            List<Future<byte[]>> futures = new ArrayList<>();
            ExecutorService executor = Executors.newFixedThreadPool(4);

            int nbBlocks = 0;
            byte[] block = new byte[blockSizeInBytes];
            int bytesRead = 0;
            while ((bytesRead = inputStream.read(block)) >= 0) {

                if (bytesRead != blockSizeInBytes) {
                    System.out.println("Did not read a full block (8 bytes != " + bytesRead + " bytes)");
                    System.out.println("Bytes in block: " + block.length);
                    // TODO ??
                }

                nbBlocks++;
                System.out.println("Encrypting block async " + nbBlocks);
                futures.add(CompletableFuture.supplyAsync(() -> {
                    try {
                        return encryptBlock(block, subkeys);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return block; // hack
                    }
                }, executor).exceptionally(ex -> {
                    throw (RuntimeException) ex;
                }));
            }

            System.out.println("Done setting tasks");

            futures.stream().forEachOrdered(encryptedBlock -> {
                try {
                    outputStream.write(encryptedBlock.get());
                    System.out.println("Blok weggeschreven");
                } catch (InterruptedException | ExecutionException | IOException e) {
                    throw new RuntimeException(e);
                }
            });

            System.out.println("Done writing to file");

            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static byte[] encryptBlock(byte[] block, byte[][] subkeys) throws Exception {
        // check of blok grootte juist, maar wat met laatste blok ???

        if (block.length != 8)
            System.out.println("Block not 8 length");

        //long millis1 = System.nanoTime();

        byte[] permutatedBlock = ByteHelper.permutate(block, initialPermutation);

        //long millis2 = System.nanoTime();
        //System.out.println("Time permutation " + (millis2 - millis1));

        byte[] prevLeft, prevRight, left, right;
        // verdeel in initiele linkse en rechtse blok
        // todo floor or ceil ? in / 2
        prevLeft = Arrays.copyOfRange(permutatedBlock, 0, (int) Math.ceil(permutatedBlock.length / 2.0));
        prevRight = Arrays.copyOfRange(permutatedBlock, permutatedBlock.length / 2, permutatedBlock.length);

        // bereken L1 R1 tem L15 R15
        for (int i = 1; i < (16 - 1); i++) {
            //System.out.println("Iteratie " + i);

            // bereken linkse en rechtse blok
            left = prevRight;

            //long millisBeforeXorFeistel = System.nanoTime();
            right = ByteHelper.xorByteBlocks(prevLeft, new Feistel().executeFunction(prevRight, subkeys[i]));
            //System.out.println("time xor feistel" + (System.nanoTime() - millisBeforeXorFeistel));

            // voorbereiding volgende iteratie
            prevLeft = left;
            prevRight = right;
        }

        // laatste (16e) iteratie is verschillend
        //System.out.println("Laatste iteratie");


        left = ByteHelper.xorByteBlocks(prevLeft, new Feistel().executeFunction(prevRight, subkeys[16 - 1]));
        right = prevLeft;

        //long millis3 = System.nanoTime();
        //System.out.println("Time iterations " + (millis3 - millis2));

        System.out.println("Blok geencrypteerd");
        return ByteHelper.permutate(concatBlocks(left, right), inverseInitialPermutation);
    }

    // based on http://stackoverflow.com/a/784842
    private static byte[] concatBlocks(byte[] left, byte[] right) {
        byte[] result = Arrays.copyOf(left, left.length + right.length);
        System.arraycopy(right, 0, result, left.length, right.length);
        return result;
    }

}