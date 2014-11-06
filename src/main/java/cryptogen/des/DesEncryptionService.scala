package cryptogen.des

import java.io._
import akka.actor._
import akka.routing.RoundRobinPool

/**
 * Asynchronous DES encryption/decryption with Actors using Scala/Akka. 
 * @author Peter Neyens
 */

case class EncryptFile(filePath: String, subKeys: Array[Array[Byte]])
case class DecryptFile(filePath: String, reversedSubKeys: Array[Array[Byte]])

case class EncryptBlock(block: Array[Byte], subKeys: Array[Array[Byte]], id: Long)
case class DecryptBlock(block: Array[Byte], subKeys: Array[Array[Byte]], id: Long)
case class EncryptedBlock(block: Array[Byte], id: Long)
case class DecryptedBlock(block: Array[Byte], id: Long)
case class AllDataEncrypted


/**
 * A DesEncryptionService can encrypt and decrypt files using the DES algorithm.
 *
 * @author Peter Neyens
 */
object DesEncryptionService {

  val system = ActorSystem()
  val worker = system.actorOf(
    props = Props[DesEncryptionWorker].withRouter(RoundRobinPool(10)),
    name = "desEncryptionService"
  )

  /**
   * Encrypt the file with the specified file path with the specified sub keys using DES.
   */
  def encryptFile(filePath: String, subKeys: Array[Array[Byte]]) = {
    val actor = system.actorOf(DesEncryptionActor.props(worker))//, name = "des-encrypt-" + filePath)
    actor ! EncryptFile(filePath, subKeys)
  }

  /**
   * Decrypt the file with the specified file path with the specified sub keys using DES.
   */
  def decryptFile(filePath: String, reversedSubKeys: Array[Array[Byte]]) = {
    val actor = system.actorOf(DesEncryptionActor.props(worker))//, name = "des-decrypt-" + filePath)
    actor ! DecryptFile(filePath, reversedSubKeys)
  }
  
  
  /**
   * Working actor which encrypts/decrypts blocks.
   */
  class DesEncryptionWorker extends Actor {
   
    /**
     * Process received messages.
     */
    def receive = {
      case EncryptBlock(block, keys, id) => {
        sender ! EncryptedBlock(DesEncryption.encryptBlock(block, keys), id)
      }
      case DecryptBlock(block, keys, id) => {
        sender ! DecryptedBlock(DesEncryption.decryptBlock(block, keys), id)
      }
    }
   
  }

  object DesEncryptionActor {
    
    /**
     * Create Props for an actor of this type.
     * @param worker The worker to be passed to this actor’s constructor.
     * @return a Props for creating this actor, which can then be further configured
     * (e.g. calling `.withDispatcher()` on it)
     */
    def props(worker: ActorRef): Props = Props(new DesEncryptionActor(worker))
  
  }

  /**
   * DesEncryptionActor which encrypts/decrypts files using DES.
   */
  class DesEncryptionActor(worker: ActorRef) extends Actor with ActorLogging {
  
    var outputStream: OutputStream = new NullOutputStream() //TODO
    var encryptedBlocks = scala.collection.mutable.MutableList[(Array[Byte], Long)]()
    var nbTotalBlocks = 0L
    var nbBytesPadding = 0

    val blockSizeInBytes = DesEncryption.blockSizeInBytes

    /**
     * Process received messages.
     */
    def receive = {
      case EncryptFile(filePath, subKeys) => encryptFile(filePath, subKeys)
      case DecryptFile(filePath, subKeys) => decryptFile(filePath, subKeys)
      case EncryptedBlock(block, id) => processEncryptedBlock(block, id)
      case DecryptedBlock(block, id) => processDecryptedBlock(block, id)
      case AllDataEncrypted => writeAllDataToFile
    }

    /**
     * Encrypt the file with the specified file path using the specified sub keys.
     */
    private def encryptFile(filePath: String, subKeys: Array[Array[Byte]]) = {
      val inputFile = new File(filePath)
      val inputStream = new BufferedInputStream(new FileInputStream(inputFile))
      val outputFile = new File(filePath + ".des")
      outputStream = new BufferedOutputStream(new FileOutputStream(outputFile))
    
      log.info(f"Encrypt file $filePath")

      val nbBytesFile = inputFile.length
      nbTotalBlocks = (nbBytesFile / blockSizeInBytes.toDouble).ceil.toLong
      log.info(f"Nb blocks $nbTotalBlocks")
      
      val nbBytesPaddingNeeded = (blockSizeInBytes - (nbBytesFile % blockSizeInBytes)).toInt
      val header = nbBytesPaddingNeeded.toByte
      outputStream.write(header)

      (1L to nbTotalBlocks).foreach( blockId => {
        var block = new Array[Byte](blockSizeInBytes)
        val bytesRead = inputStream.read(block)
        worker ! EncryptBlock(block, subKeys, blockId)
      })

      inputStream.close
    }

    /**
     * Decrypt the file with the specified file path using the specified sub keys.
     */
    private def decryptFile(filePath: String, subKeys: Array[Array[Byte]]) = {
      val inputFile = new File(filePath)
      val inputStream = new BufferedInputStream(new FileInputStream(inputFile))
      val outputFile = new File(filePath.replace(".des", ".decrypt"))
      outputStream = new BufferedOutputStream(new FileOutputStream(outputFile))

      log.info(f"Decrypt file $filePath")

      val nbBytesFileWithoutHeader = inputFile.length - 1
      nbTotalBlocks = Math.ceil(nbBytesFileWithoutHeader / blockSizeInBytes.toDouble).toLong
      log.info(f"Nb blocks $nbTotalBlocks") 
      nbBytesPadding = inputStream.read.toInt
     
      (1L to nbTotalBlocks).foreach( blockId => {
        var block = new Array[Byte](blockSizeInBytes)
        val bytesRead = inputStream.read(block)
        worker ! DecryptBlock(block, subKeys, blockId)
      })

      inputStream.close
    }

    /**
     * Save the encrypted block.
     */
    private def processEncryptedBlock(block: Array[Byte], id: Long) = processBlock(block, id, true)
    
    /**
     * Save the decrypted block.
     */
    private def processDecryptedBlock(block: Array[Byte], id: Long) = processBlock(block, id, false)

    /**
     * Save the block.
     */
    private def processBlock(block: Array[Byte], id: Long, encryption: Boolean) =  {
      // delete padding from last block if decrypting
      (encryption, nbTotalBlocks) match {
        case (false, `id`)  => {
          val blockWithoutPadding = block.slice(0, blockSizeInBytes - nbBytesPadding)
          (blockWithoutPadding, id) +=: encryptedBlocks
        }
        case _ => (block, id) +=: encryptedBlocks
      }

      // log progress
      val tenPercent = nbTotalBlocks / 10;
      if (tenPercent != 0 && encryptedBlocks.length % tenPercent == 0) {
        val percent = 10 * (encryptedBlocks.length / tenPercent)
        log.info(f"$percent %%")
      }

      // all blocks are encrypted/decrypted
      if (encryptedBlocks.length == nbTotalBlocks) {
        self ! AllDataEncrypted
      }
    }
    
    /**
     * Write all the encrypted/decrypted blocks to the output file.
     */
    private def writeAllDataToFile = {
      log.info(f"Write file")
      encryptedBlocks.sortWith(_._2 < _._2).map(_._1).foreach(outputStream.write)
      outputStream.close
      
      context.stop(self)
    }
  }
   
}

/**
 * OutputStream with no functionality
 */
class NullOutputStream extends OutputStream {
  def write(b: Int) = {}
}
