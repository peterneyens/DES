/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.teamawesome.des;

import java.util.Iterator;

/**
 *
 * @author arno
 * source: http://stackoverflow.com/questions/1034473/java-iterate-bits-in-byte-array
 */
public class ByteArrayBitIterable implements Iterable<Boolean> {
    private final byte[] array;

    public ByteArrayBitIterable(byte[] array) {
        this.array = array;
    }

    public Iterator<Boolean> iterator() {
        return new Iterator<Boolean>() {
            private int bitIndex = 0;
            private int arrayIndex = 0;

            public boolean hasNext() {
                return (arrayIndex < array.length) && (bitIndex < 8);
            }

            public Boolean next() {
                Boolean val = (array[arrayIndex] >> (7 - bitIndex) & 1) == 1;
                bitIndex++;
                if (bitIndex == 8) {
                    bitIndex = 0;
                    arrayIndex++;
                }
                return val;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
