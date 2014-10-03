package cryptogen;

import helpers.ByteHelper;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
        37, 5, 45, 13, 53, 21, 61, 29,
        36, 4, 44, 12, 52, 20, 60, 28,
        35, 3, 43, 11, 51, 19, 59, 27,
        34, 2, 42, 10, 50, 18, 58, 26,
        33, 1, 41, 9, 49, 17, 57, 25
    };

    public static void encryptFile(String filePath, String key) {
        // eerste 64 bits van key omgezet in bytes
        byte[] keyInBytes = Arrays.copyOfRange(key.getBytes(Charset.forName("UTF-8")), 0, 8);
        //System.out.println("key in bytes : " + keyInBytes);

        // TODO key checking
        // 8th bit now not used, -> pariteitsbit (bytes hebben oneven aantal 1)
        // TODO check keyInBytes niet 00000000 of 111111111, of subkeys vaak gelijk, ...
        byte[][] subKeys = new KeyCalculator().Generate(keyInBytes);

        byte[][] reversedSubKeys = reverseArray(subKeys);

        long before = System.nanoTime();
        encryptFile(filePath, subKeys);
        decryptFile(filePath + ".des", reversedSubKeys);
        long afterSync = System.nanoTime();
        encryptFileAsync(filePath, subKeys);
        decryptFileAsync(filePath + ".des2", reversedSubKeys);
        long afterAsync = System.nanoTime();

        System.out.println("Sync " + (afterSync - before) + " Async " + (afterAsync - afterSync));

        //for(byte[] bytes : subKeys) { ByteHelper.printByteArray(bytes); }
        //System.out.println();

        //byte[][] reversedSubKeys = reverseArray(subKeys);

        //for(byte[] bytes : reversedSubKeys) { printByteArray(bytes); }
        //System.out.println();

        //decryptFile(filePath + ".des", reversedSubKeys);
        //decryptFile(filePath + ".des2", reversedSubKeys);
    }

    private static void encryptFile(String filePath, byte[][] subKeys) {

        try {
            File inputFile = new File(filePath);
            File outputFile = new File(filePath + ".des");
            if(! outputFile.exists()) {
                outputFile.createNewFile();
            }

            InputStream inputStream = new BufferedInputStream(new FileInputStream(inputFile));
            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

            long nbBytesFile = inputFile.length();
            long nbTotalBlocks = (long) Math.ceil(nbBytesFile / (double) blockSizeInBytes);
            int nbBytesPaddingNeeded = (int) (blockSizeInBytes - (nbBytesFile % blockSizeInBytes));

            byte header = (byte) nbBytesPaddingNeeded;
            System.out.println("header " + header);
            outputStream.write(header);

            //int nbBlocks = 0;
            byte[] block = new byte[blockSizeInBytes];
            int bytesRead = 0;
            //while ((bytesRead = inputStream.read(block)) >= 0) {
            for (int nbBlocks = 1; nbBlocks <= nbTotalBlocks; nbBlocks++) {

                bytesRead = inputStream.read(block);
                if (bytesRead != blockSizeInBytes) {
                    System.out.println("Did not read a full block");
                    System.out.println("Bytes in block: " + block.length);
                    // TODO ??
                }


                //nbBlocks++;
                System.out.println("Encrypting block " + nbBlocks);
                byte[] encryptedBlock = encryptBlock(block, subKeys);

                ByteHelper.printByteArray(block);
                ByteHelper.printByteArray(encryptedBlock);

                // schrijf geencrypteerd blok weg naar output bestand
                outputStream.write(encryptedBlock);

                // remove later
                block = new byte[blockSizeInBytes];
            }

            System.out.println("nbTotalBlocks " + nbTotalBlocks);
            System.out.println("nbBytesPaddingNeeded " + nbBytesPaddingNeeded);

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
            File inputFile = new File(filePath);
            File outputFile = new File(filePath.replace(".des",".decrypted"));
            if(! outputFile.exists()) {
                outputFile.createNewFile();
            }

            InputStream inputStream = new BufferedInputStream(new FileInputStream(inputFile));
            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

            long nbBytesFileWithoutHeader = inputFile.length() - 1;
            long nbTotalBlocks = (long) Math.ceil(nbBytesFileWithoutHeader / (double) blockSizeInBytes);

            int nbBytesHeading = inputStream.read();
            System.out.println("padding " + nbBytesHeading);

            byte[] block = new byte[blockSizeInBytes];
            int bytesRead = 0;
            for (int nbBlocks = 1; nbBlocks <= nbTotalBlocks; nbBlocks++) {

                bytesRead = inputStream.read(block);
                if (bytesRead != blockSizeInBytes) {
                    System.out.println("Did not read a full block");
                    System.out.println("Bytes in block: " + bytesRead);
                    // TODO ??
                }

                System.out.println("Decrypting block " + nbBlocks);
                byte[] decryptedBlock = decryptBlock(block, reversedSubKeys);

                ByteHelper.printByteArray(block);

                // schrijf geencrypteerd blok weg naar output bestand
                // laatste blok => verwijder padding
                if (nbBlocks == nbTotalBlocks) {
                    byte[] blockWithoutPadding = Arrays.copyOfRange(decryptedBlock, 0, blockSizeInBytes - nbBytesHeading);
                    outputStream.write(blockWithoutPadding);
                    ByteHelper.printByteArray(blockWithoutPadding);
                } else {
                    outputStream.write(decryptedBlock);
                    ByteHelper.printByteArray(decryptedBlock);
                }

                // remove later
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

    private static void encryptFileAsync(String filePath, byte[][] subKeys) {

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

            long nbBytesFile = inputFile.length();
            long nbTotalBlocks = (long) Math.ceil(nbBytesFile / (double) blockSizeInBytes);
            int nbBytesPaddingNeeded = (int) (blockSizeInBytes - (nbBytesFile % blockSizeInBytes));

            byte header = (byte) nbBytesPaddingNeeded;
            System.out.println("header " + header);
            outputStream.write(header);

            long before = System.nanoTime();

            byte[] block = new byte[blockSizeInBytes];
            int bytesRead = 0;
            for (int nbBlocks = 1; nbBlocks <= nbTotalBlocks; nbBlocks++) {

                bytesRead = inputStream.read(block);
                if (bytesRead != blockSizeInBytes) {
                    System.out.println("Did not read a full block");
                    System.out.println("Bytes in block: " + block.length);
                    // TODO ??
                }

                final byte[] finalBlock = block;
                System.out.println("Encrypting block async " + nbBlocks);
                futures.add(CompletableFuture.supplyAsync(() -> {
                    try {
                        return encryptBlock(finalBlock, subKeys);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return finalBlock; // hack
                    }
                }, executor).exceptionally(ex -> {
                    throw (RuntimeException) ex;
                }));

                // remove later
                block = new byte[blockSizeInBytes];
            }
            System.out.println("Done setting tasks");
            long afterTasks = System.nanoTime();

            futures.stream().forEachOrdered(encryptedBlock -> {
                try {
                    outputStream.write(encryptedBlock.get());
                    System.out.println("Blok weggeschreven");
                } catch (InterruptedException | ExecutionException | IOException e) {
                    throw new RuntimeException(e);
                }
            });
            long afterWriting = System.nanoTime();

            System.out.println("Done writing to file");

            System.out.println("Setting tasks " + (afterTasks - before) + " Writing " + (afterWriting - afterTasks));

            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void decryptFileAsync(String filePath, byte[][] reversedSubKeys) {

        System.out.println();
        System.out.println("Decrypting");

        try {
            File inputFile = new File(filePath);
            File outputFile = new File(filePath.replace(".des2",".decryptedasync"));
            if(! outputFile.exists()) {
                outputFile.createNewFile();
            }

            InputStream inputStream = new BufferedInputStream(new FileInputStream(inputFile));
            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

            List<Future<byte[]>> futures = new ArrayList<>();
            ExecutorService executor = Executors.newFixedThreadPool(4);

            long nbBytesFileWithoutHeader = inputFile.length() - 1;
            long nbTotalBlocks = (long) Math.ceil(nbBytesFileWithoutHeader / (double) blockSizeInBytes);

            int nbBytesHeading = inputStream.read();
            System.out.println("padding " + nbBytesHeading);

            byte[] block = new byte[blockSizeInBytes];
            int bytesRead = 0;
            for (int nbBlocks = 1; nbBlocks <= nbTotalBlocks; nbBlocks++) {

                bytesRead = inputStream.read(block);
                if (bytesRead != blockSizeInBytes) {
                    System.out.println("Did not read a full block");
                    System.out.println("Bytes in block: " + block.length);
                    // TODO ??
                }

                final byte[] finalBlock = block;
                System.out.println("Encrypting block async " + nbBlocks);
                futures.add(CompletableFuture.supplyAsync(() -> {
                    try {
                        return decryptBlock(finalBlock, reversedSubKeys);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return finalBlock; // hack
                    }
                }, executor).exceptionally(ex -> {
                    throw (RuntimeException) ex;
                }));

                // remove later
                block = new byte[blockSizeInBytes];
            }

            //futures.stream().forEachOrdered(encryptedBlock -> {
            //});

            for (int i = 1; i <= nbTotalBlocks; i++) {
                byte[] decryptedBlock = futures.get(i-1).get();
                if  (i == nbTotalBlocks) {
                    decryptedBlock = Arrays.copyOfRange(decryptedBlock, 0, blockSizeInBytes - nbBytesHeading);
                }
                try {
                    outputStream.write(decryptedBlock);
                    System.out.println("Blok weggeschreven");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
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



    public static byte[] encryptBlock(byte[] block, byte[][] subkeys) throws Exception {
        // check of blok grootte juist, maar wat met laatste blok ???

        if (block.length != 8)
            System.out.println("Block not 8 length");

        //long millis1 = System.nanoTime();

        byte[] permutatedBlock = ByteHelper.permutFunc(block, initialPermutation);

        //long millis2 = System.nanoTime();
        //System.out.println("Time permutation " + (millis2 - millis1));

        byte[] prevLeft, prevRight, left, right;
        // verdeel in initiele linkse en rechtse blok
        prevLeft = Arrays.copyOfRange(permutatedBlock, 0, (int) Math.ceil(permutatedBlock.length / 2.0));
        prevRight = Arrays.copyOfRange(permutatedBlock, permutatedBlock.length / 2, permutatedBlock.length);

        //System.out.println("Iteratie 0");
        //ByteHelper.printByteArray(prevLeft);
        //ByteHelper.printByteArray(prevRight);

        // bereken L1 R1 tem L15 R15
        for (int i = 1; i <= 16; i++) {

            // bereken linkse en rechtse blok
            left = prevRight;

            //long millisBeforeXorFeistel = System.nanoTime();
            right = ByteHelper.xorByteBlocks(prevLeft, new Feistel().executeFunction(prevRight, subkeys[i-1]));
            //System.out.println("time xor feistel" + (System.nanoTime() - millisBeforeXorFeistel));

            //System.out.println();
            //System.out.println("Iteratie " + i);
            //ByteHelper.printByteArray(left);
            //ByteHelper.printByteArray(right);
            //System.out.println("------------------------------------------------------");

            // voorbereiding volgende iteratie
            prevLeft = left;
            prevRight = right;
        }

        // swap voor laatste iteratie
        left = prevRight;
        right = prevLeft;

        //long millis3 = System.nanoTime();
        //System.out.println("Time iterations " + (millis3 - millis2));

        System.out.println("Blok geencrypteerd");
        return ByteHelper.permutFunc(concatBlocks(left, right), inverseInitialPermutation);

    }

    // subkeys need to be reversed (vs encryptBlock)
    public static byte[] decryptBlock(byte[] block, byte[][] subKeys) throws Exception {
        return encryptBlock(block, subKeys);
    }

    private static <T> T[] reverseArray(T[] array) {
        // reverse array (reversing list reverses array <-- http://stackoverflow.com/a/12893811)
        Collections.reverse(Arrays.asList(array));
        return array;
    }

    // based on http://stackoverflow.com/a/784842
    private static byte[] concatBlocks(byte[] left, byte[] right) {
        byte[] result = Arrays.copyOf(left, left.length + right.length);
        System.arraycopy(right, 0, result, left.length, right.length);
        return result;
    }

}