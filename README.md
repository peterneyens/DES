CRYPTOGEN
=========

A Java/Scala implementation of the [DES algorithm](http://en.wikipedia.org/wiki/Data_Encryption_Standard "DES algorithm on Wikipedia") and a  [Steganography](http://en.wikipedia.org/wiki/Steganography) function for a course on network and data security.

DES
---

In the DES algorithm a file is split in multiple blocks of data, and these blocks of data are encrypted individually. Five different DesService are included which use a different method of concurrency to encrypt or decrypt these blocks.

The four DesService are 
- a synchronous DES implementation
- an asynchronous version using Java 8 CompletableFutures 
- another asynchronous version in Scala using Akka actors
- a (naive) distributed version in Scala using Akka Cluster
- and a version in Scala using (the currently experimental) Akka Streams.


Steganography
-------------

The Steganography uses the least significant bit method and can encode/decode data in a BMP image.


Requirements
------------

Java 8 and SBT


Run
---

    $ sbt "run-main cryptogen.CryptoGen"
