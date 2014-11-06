package cryptogen.des;

import helpers.ByteHelper;
import org.junit.*;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

/**
 * Created by peter on 1/10/14.
 */
public class FeistelTest {

    @BeforeClass
    public static void setUpClass() {

    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    //
    // K1:
    // 000110 110000 001011 101111 111111 000111 000001 110010
    //
    // E(R0):
    //        011110 100001 010101 010101 011110 100001 010101 010101
    //
    // K1 XOR E(R0):
    //        011000 010001 011110 111010 100001 100110 010100 100111
    //
    //
    // S(B1)S(B2)S(B3)S(B4)S(B5)S(B6)S(B7)S(B8):
    // 0101 1100 1000 0010 1011 0101 1001 0111
    //
    //
    // permutatie
    // 0010 0011 0100 1010 1010 1001 1011 1011
    @Test
    public void testFeistel() {

        byte[] subKey1 = ByteHelper.convertBinaryStringToByteArray("000110 110000 001011 101111 111111 000111 000001 110010");
        byte[] blockR0 = ByteHelper.convertBinaryStringToByteArray("11110000 10101010 11110000 10101010");

        byte[] expectedResult = ByteHelper.convertBinaryStringToByteArray("0010 0011 0100 1010 1010 1001 1011 1011");

        try {
            byte[] result = Feistel.executeFunction(blockR0, subKey1);

            System.out.println();
            System.out.println("Feistel expected vs actual result");
            ByteHelper.printByteArray(expectedResult);
            ByteHelper.printByteArray(result);

            assertArrayEquals(expectedResult, result);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }


}
