package cryptogen.des

import java.io._
import akka.actor._
import language.postfixOps
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration._
import akka.actor._
import akka.actor.ActorRef
import akka.pattern.pipe
import akka.contrib.pattern.ClusterSingletonManager
import akka.contrib.pattern.ClusterSingletonProxy
import akka.routing.FromConfig
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import clusterbalancing._

/**
 * A DesEncryptionService can encrypt and decrypt files using the DES algorithm.
 *
 * @author Peter Neyens
 */
class DistributedDesService extends AbstractDesService {

  /**
   * Encrypt the file with the specified file path with the specified sub keys using DES.
   */
  def encryptFile(filePath: String, subKeys: Array[Array[Array[Byte]]]) = {
    val actor = DistributedDesService.system.actorOf(AkkaDesService.DesEncryptionActor.props(DistributedDesService.workMasterRef))
    actor ! EncryptFile(filePath, subKeys)
  }

  /**
   * Decrypt the file with the specified file path with the specified sub keys using DES.
   */
  def decryptFile(filePath: String, reversedSubKeys: Array[Array[Array[Byte]]]) = {
    val actor = DistributedDesService.system.actorOf(AkkaDesService.DesEncryptionActor.props(DistributedDesService.workMasterRef))
    actor ! DecryptFile(filePath, reversedSubKeys)
  }
 
}

object DistributedDesService {

  val port = "2551"
  val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
    withFallback(ConfigFactory.parseString("akka.cluster.roles = [compute]")).
    withFallback(ConfigFactory.load())

  val system = ActorSystem("ClusterSystem", config)

  system.actorOf(ClusterSingletonManager.props(
    singletonProps = Props[Master],
    singletonName = "workMaster",
    terminationMessage = PoisonPill,
    role = Some("compute")
  ), name = "singleton")

  system.actorOf(ClusterSingletonProxy.props(
    singletonPath = "/user/singleton/workMaster",
    role = Some("compute")
  ), name = "workMasterProxy")

  val workMaster = system.actorSelection("/user/workMasterProxy")
  val timeout = Timeout(1 second)
  val workMasterFut = workMaster.resolveOne(timeout.duration)
  val workMasterRef = Await.result(workMasterFut, timeout.duration).asInstanceOf[ActorRef]

  /**
   * Encrypt the file with the specified file path with the specified sub keys using DES.
   */
  //def encryptFile(filePath: String, subKeys: Array[Array[Byte]]) = {
  //  val actor = system.actorOf(DesEncryptionActor.props(workMasterRef))
  //  actor ! EncryptFile(filePath, subKeys)
  //}

  /**
   * Decrypt the file with the specified file path with the specified sub keys using DES.
   */
  //def decryptFile(filePath: String, reversedSubKeys: Array[Array[Byte]]) = {
  //  val actor = system.actorOf(DesEncryptionActor.props(workMasterRef))
  //  actor ! DecryptFile(filePath, reversedSubKeys)
  //}
  
  
  /**
   * Working actor which encrypts/decrypts blocks.
   */
  class DesEncryptionWorker extends Worker {
  
    // We'll use the current dispatcher for the execution context.
    // You can use whatever you want.
    implicit val ec = context.dispatcher

    def doWork(workSender: ActorRef, msg: Any): Unit = msg match {
      case EncryptBlock(block, keys, id) => 
        Future {
          val encrypted = keys.foldLeft(block){ (block, subkeys) =>
            DesAlgorithm.encryptBlock(block, subkeys)
          }
          workSender ! EncryptedBlock(encrypted, id)
          WorkComplete("done")
        } pipeTo self
      case DecryptBlock(block, keys, id) => 
        Future {
          val decrypted = keys.foldLeft(block){ (block, subkeys) =>
            DesAlgorithm.decryptBlock(block, subkeys)
          }
          workSender ! DecryptedBlock(decrypted, id)
          WorkComplete("done")
        } pipeTo self
    }
   
  }

}
