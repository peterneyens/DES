package cryptogen.des

import java.io._

import akka.actor.ActorSystem
import akka.stream.ActorFlowMaterializer
import akka.stream.io.Implicits._
import akka.stream.scaladsl._
import akka.util.ByteString

import scala.util.{Failure, Success}
import scala.concurrent.Future


object StreamDesService extends AbstractDesService {
  implicit val system = ActorSystem("sys")
  import system.dispatcher
  implicit val materializer = ActorFlowMaterializer()

  private val SIZE = DesAlgorithm.blockSizeInBytes

  /**
   * Encrypt the file with the specified file path with the specified sub keys using DES.
   */
  override def encryptFile(filePath: String, subKeys: Array[Array[Array[Byte]]]) : Unit = {
    def encryptBlock(block: Array[Byte]) = Future { DesAlgorithm.encryptBlock(block, subKeys) }  

    def calculateHeader(file: File) = {
      val nbBytesFile = file.length     
      val nbBytesPaddingNeeded = (SIZE - (nbBytesFile % SIZE)).toInt
      nbBytesPaddingNeeded.toByte
    }

    val start = System.nanoTime
    val from = new File(filePath)  	
    val toFileName = filePath + ".des"

    // write header to output file
    val output = new FileOutputStream(toFileName)
    try {
      val header = calculateHeader(from)
      output.write(header)
    } finally output.close()

    val to = new File(toFileName)

    val r: Future[Long] = Source.synchronousFile(from, chunkSize = SIZE)
      .map(bytes => bytes.toArray ++ Array.fill(SIZE - bytes.length)(0.toByte))
      .mapAsync(32)(encryptBlock)
      .map(ByteString(_))
      .toMat(Sink.synchronousFile(to, append = true))((_, bytesWritten) => bytesWritten)//(Keep.right)
      .run()

    r.onComplete {
      case Success(_) => 
        println(s"Successfully encrypted file ${filePath.split('/').last}.")
        println(s"[Elapsed time = ${(System.nanoTime - start) / 1000000} millis]")
      case Failure(e) => 
        println(e.getMessage)
    }
  }


  /**
   * Decrypt the file with the specified file path with the specified sub keys using DES.
   */
  override def decryptFile(filePath: String, reversedSubKeys: Array[Array[Array[Byte]]]) : Unit = {
    def decryptBlock(block: Array[Byte]) = Future { DesAlgorithm.decryptBlock(block, reversedSubKeys) }  

    def binStream(in:BufferedInputStream) = Stream.continually{
      var block = Array.fill(SIZE)(0.toByte)
      val nbBytesRead = in.read(block)
      (nbBytesRead, block)
    }.takeWhile(_._1 != -1).map(_._2)

    val start = System.nanoTime
    val from = new File(filePath)   
    val to = new File(filePath.replace(".des",".decryptedstream"))
    val inputStream = new BufferedInputStream(new FileInputStream(from))     
    val nbBytesPadding = inputStream.read()

    val r = Source(() => binStream(inputStream).iterator)
      .mapAsync(32)(decryptBlock)
      .map(ByteString(_))
      .toMat(Sink.synchronousFile(to))((_, bytesWritten) => bytesWritten)
      .run()

    r.onComplete {
      case Success(_) => 
        println(s"Successfully decrypted file ${filePath.split('/').last}.")
        println(s"[Elapsed time = ${(System.nanoTime - start) / 1000000} millis]")
        inputStream.close()
      case Failure(e) => 
        println(e.getMessage)
        inputStream.close()
    }

  }

  /** Close the StreamDesService */
  override def close : Unit = system.shutdown()

}