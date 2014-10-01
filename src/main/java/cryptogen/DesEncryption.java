package cryptogen;

import helpers.ByteHelper;
import helpers.External;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Created by peter on 22/09/14.
 */
public class DesEncryption {

    public static enum opperation {

        ENCRYPT, DECRYPT
    }

    private static final int blockSizeInBytes = 8;

    private static final int[] initialPermutation = new int[]{
        58, 50, 42, 34, 26, 18, 10, 2,
        60, 52, 44, 36, 28, 20, 12, 4,
        62, 54, 46, 38, 30, 22, 14, 6,
        64, 56, 48, 40, 32, 24, 16, 8,
        57, 49, 41, 33, 25, 17, 9, 1,
        59, 51, 43, 35, 27, 19, 11, 3,
        61, 53, 45, 37, 29, 21, 13, 5,
        63, 55, 47, 39, 31, 23, 15, 7
    };

    private static final int[] inverseInitialPermutation = new int[]{
        40, 8, 48, 16, 56, 24, 64, 32,
        39, 7, 47, 15, 55, 23, 63, 31,
        38, 6, 46, 14, 54, 22, 62, 30,
        37, 5, 45, 13, 53, 21, 61, 29,
        36, 4, 44, 12, 52, 20, 60, 28,
        35, 3, 43, 11, 51, 19, 59, 27,
        34, 2, 42, 10, 50, 18, 58, 26,
        33, 1, 41, 9, 49, 17, 57, 25
    };

    /**
     * High level interface for encryption/decryption
     *
     * @param filePath
     * @param key
     * @param opperation
     */
    public static void encryptOrDecryptFile(String filePath, String key, Enum opperation) {
        // eerste 64 bits van key omgezet in bytes
        byte[] keyInBytes = Arrays.copyOfRange(key.getBytes(Charset.forName("UTF-8")), 0, 8);
        System.out.println("key in bytes : " + keyInBytes.length);

        // TODO key checking
        // 8th bit now not used, -> pariteitsbit (bytes hebben oneven aantal 1)
        // TODO check keyInBytes niet 00000000 of 111111111, of subkeys vaak gelijk, ...
//        long before = System.nanoTime();
        encryptOrDecryptFile(filePath, keyInBytes, opperation);
//        long afterSync = System.nanoTime();
        // encryptFileAsync(filePath, keyInBytes);
//        long afterAsync = System.nanoTime();

//        System.out.println("Sync " + (afterSync - before) + " Async " + (afterAsync - afterSync));
    }

    private static void encryptOrDecryptFile(String inputPath, byte[] keyInBytes, Enum opperation) {

        try {

            byte[][] subkeys = KeyCalculator.Generate(keyInBytes);
//            byte[][] subkeys = External.getSubkeys(keyInBytes);
            System.out.println("Subkeys gegenereerd");

            String outputPath;

            if (opperation == DesEncryption.opperation.ENCRYPT) {
                outputPath = inputPath + ".des"; // save with .des extension
            } else {
                outputPath = inputPath.substring(0, inputPath.indexOf(".des")); // remove .des extension
            }

            byte[] inputBytes = ByteHelper.fileToBytes(inputPath);
            int remainingBytes = 0,
                    paddingSize = 0;
            byte[] outputBytes = null;

            if (opperation == DesEncryption.opperation.ENCRYPT) {
                remainingBytes = inputBytes.length % 8;
                paddingSize = 8 - remainingBytes;

                outputBytes = new byte[inputBytes.length + paddingSize];
            } else {
                outputBytes = new byte[inputBytes.length];
            }

            // Loop through inputBytes and build up outputBytes
            for (int i = 0; i + 8 <= inputBytes.length; i += 8) {

                byte[] currentBlock = null;
                currentBlock = Arrays.copyOfRange(inputBytes, i, i + 8);

                System.out.println("Encrypting block " + (i + 1));
                byte[] cryptedBlock = encryptOrDecryptBlock(currentBlock, subkeys, opperation);

                // merge with outputBytes
                System.arraycopy(cryptedBlock, 0, outputBytes, i, cryptedBlock.length);
            }

            if (opperation == DesEncryption.opperation.ENCRYPT)
                addPadding(outputBytes, inputBytes, remainingBytes, paddingSize, subkeys);
            else
                outputBytes = removePadding(outputBytes);

            ByteHelper.bytesTofile(outputBytes, outputPath);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addPadding(byte[] outputBytes, byte[] inputBytes, int remainingBytes, int paddingSize, byte[][] subkeys) throws Exception {

        byte[] currentBlock = new byte[8];
        // copy any remaing bytes to the currentBlock
        System.arraycopy(inputBytes, inputBytes.length - remainingBytes, currentBlock, 0, remainingBytes);
        // add paddingSize in last byte
        currentBlock[currentBlock.length - 1] = (byte) paddingSize;
        
        byte[] cryptedBlock = encryptOrDecryptBlock(currentBlock, subkeys, DesEncryption.opperation.ENCRYPT);

        // merge with outputBytes
        System.arraycopy(cryptedBlock, 0, outputBytes, outputBytes.length - 8, cryptedBlock.length);
    }
    
    private static byte[] removePadding(byte[] outputBytes) {
        // get padding size
        int paddingSizeToBeRemoved = outputBytes[outputBytes.length - 1];
        // remove padding
        return Arrays.copyOf(outputBytes, outputBytes.length - paddingSizeToBeRemoved);
    }

//    private static void encryptFileAsync(String filePath, byte[] keyInBytes) {
//
//        byte[][] subkeys = new KeyCalculator().Generate(keyInBytes);
//        System.out.println("Subkeys gegenereerd");
//
//        //InputStream inputStream = null;
//        //OutputStream outputStream = null;
//
//        try {
//            File inputFile = new File(filePath);
//            File outputFile = new File(filePath + ".des2");
//            if (!outputFile.exists()) {
//                outputFile.createNewFile();
//            }
//
//            InputStream inputStream = new BufferedInputStream(new FileInputStream(inputFile));
//            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));
//
//            List<Future<byte[]>> futures = new ArrayList<>();
//            ExecutorService executor = Executors.newFixedThreadPool(4);
//
//            int nbBlocks = 0;
//            byte[] block = new byte[blockSizeInBytes];
//            int bytesRead = 0;
//            while ((bytesRead = inputStream.read(block)) >= 0) {
//
//                if (bytesRead != blockSizeInBytes) {
//                    System.out.println("Did not read a full block (8 bytes != " + bytesRead + " bytes)");
//                    System.out.println("Bytes in block: " + block.length);
//                    // TODO ??
//                }
//
//                nbBlocks++;
//                System.out.println("Encrypting block async " + nbBlocks);
//                futures.add(CompletableFuture.supplyAsync(() -> {
//                    try {
//                        return encryptBlock(block, subkeys);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        return block; // hack
//                    }
//                }, executor).exceptionally(ex -> {
//                    throw (RuntimeException) ex;
//                }));
//            }
//
//            System.out.println("Done setting tasks");
//
//            futures.stream().forEachOrdered(encryptedBlock -> {
//                try {
//                    outputStream.write(encryptedBlock.get());
//                    System.out.println("Blok weggeschreven");
//                } catch (InterruptedException | ExecutionException | IOException e) {
//                    throw new RuntimeException(e);
//                }
//            });
//
//            System.out.println("Done writing to file");
//
//            inputStream.close();
//            outputStream.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
    private static byte[] encryptOrDecryptBlock(byte[] block, byte[][] subkeys, Enum opperation) throws Exception {
        // check of blok grootte juist, maar wat met laatste blok ???

        if (block.length != 8) {
            System.out.println("Block not 8 length");
        }

        //long millis1 = System.nanoTime();
        byte[] permutatedBlock = ByteHelper.permutate(block, initialPermutation);

        //long millis2 = System.nanoTime();
        //System.out.println("Time permutation " + (millis2 - millis1));
        byte[] prevLeft, prevRight, left, right;

        // verdeel in initiele linkse en rechtse blok
        prevLeft = Arrays.copyOfRange(permutatedBlock, 0, 4);
        prevRight = Arrays.copyOfRange(permutatedBlock, 4, 8);

        // bereken L1 R1 tem L15 R15
        for (int i = 0; i < 16; i++) {
            //System.out.println("Iteratie " + i);

            // bereken linkse en rechtse blok
            left = prevRight;

            //long millisBeforeXorFeistel = System.nanoTime();
            if (opperation == DesEncryption.opperation.ENCRYPT) {
                right = ByteHelper.xorByteBlocks(prevLeft, new Feistel().executeFunction(prevRight, subkeys[i]));
            } else {
                right = ByteHelper.xorByteBlocks(prevLeft, new Feistel().executeFunction(prevRight, subkeys[16 - i - 1]));
            }

            //System.out.println("time xor feistel" + (System.nanoTime() - millisBeforeXorFeistel));
            // voorbereiding volgende iteratie
            prevLeft = left;
            prevRight = right;
        }

        // laatste (16e) iteratie worden de left en right blokken omgedraaid
        byte[] rightLeft = concatBlocks(prevRight, prevLeft);

        //long millis3 = System.nanoTime();
        //System.out.println("Time iterations " + (millis3 - millis2));
        System.out.println("Blok geencrypteerd");
        return ByteHelper.permutate(rightLeft, inverseInitialPermutation);
    }

    // based on http://stackoverflow.com/a/784842
    private static byte[] concatBlocks(byte[] left, byte[] right) {
        byte[] result = Arrays.copyOf(left, left.length + right.length);
        System.arraycopy(right, 0, result, left.length, right.length);
        return result;
    }

}
