package Filter

import java.nio.ByteBuffer
import scala.collection.immutable.Vector


import NewHDL.Core.PCIEInterface

object FilterPCIEInterface {

  val HOSTIN_OFFSET = 512
  val HOSTOUT_OFFSET = 514
  val TSTATE_OFFSET = 768
  val CLIENTIN_OFFSET = 515
  val CLIENTOUT_OFFSET = 513
  val STATEIN_OFFSET = 516
  val STATEOUT_OFFSET = 517
  val FSTATE_OFFSET = 518
  val PROBE_OFFSET = 519
  val IDXIN_OFFSET = 520
  val IDXOUT_OFFSET = 521
  def startTransaction = {
    PCIEInterface.startTransaction

  }
  def endTransaction = PCIEInterface.endTransaction
  def filter(a:Array[Long]) : Array[Long] = {
    val buf = ByteBuffer.allocate(4096)

    var res = Vector[Long]()
    val t = ByteBuffer.allocate(8)
    var hostin:Long = 0
    var hostout:Long = 0
    var start:Long = 0
    val n = (a.length - 1) / 512 + 1

    //init
    t.putLong(0,hostin)
    PCIEInterface.writeBytes(HOSTIN_OFFSET * 8,t.array())
    t.putLong(0,hostout)
    PCIEInterface.writeBytes(HOSTOUT_OFFSET * 8,t.array())
    t.putLong(0,start)
    PCIEInterface.writeBytes(TSTATE_OFFSET * 8,t.array())

    var ret = new Array[Byte](0)
    var clientin:Long = 0
    var clientout:Long = 0

    for (i <- 0 until n) {
      buf.clear()
      for (l <- a.slice(i*512,(i+1)*512)) {
        buf.putLong(l)
      }
      ret = PCIEInterface.readBytes(CLIENTIN_OFFSET * 8,8)
      clientin = ByteBuffer.wrap(ret).getLong(0)

      clientout = 0
      while (hostin == clientin) {
        ret = PCIEInterface.readBytes(CLIENTOUT_OFFSET * 8,8)
        clientout = ByteBuffer.wrap(ret).getLong(0)
        if (clientout != hostout) {

          ret = PCIEInterface.readBytes(0,512 * 8)
          val resbuf = ByteBuffer.wrap(ret)
          for (k <- 0 until 512) {
            res = res :+ resbuf.getLong(k*8)
          }
          hostout = clientout
          t.putLong(0,hostout)
          PCIEInterface.writeBytes(HOSTOUT_OFFSET * 8,t.array())
        }
        ret = PCIEInterface.readBytes(CLIENTIN_OFFSET * 8,8)
        clientin = ByteBuffer.wrap(ret).getLong(0)
      }
      PCIEInterface.writeBytes(0,buf.array())
      if (i == n - 1) {
        start = 1
        t.putLong(0, start)
        PCIEInterface.writeBytes(TSTATE_OFFSET * 8, t.array())
      }
      hostin = clientin
      t.putLong(0,hostin)
      PCIEInterface.writeBytes(HOSTIN_OFFSET * 8, t.array())
    }
    ret = PCIEInterface.readBytes(CLIENTOUT_OFFSET * 8,8)
    clientout = ByteBuffer.wrap(ret).getLong(0)
    if (clientout != hostout) {

      ret = PCIEInterface.readBytes(0,512 * 8)
      val size = ByteBuffer.wrap(PCIEInterface.readBytes(IDXOUT_OFFSET * 8,8)).getLong().toInt
      val resbuf = ByteBuffer.wrap(ret)
      //resbuf.position(0)
      for (k <- 0 until size) {
        res = res :+ resbuf.getLong(k*8)

      }
      hostout = clientout
      t.putLong(0,hostout)
      PCIEInterface.writeBytes(HOSTOUT_OFFSET * 8,t.array())
    }
    res.toArray
  }



}
//test

object Main {
  def main(args: Array[String]): Unit = {
    FilterPCIEInterface.startTransaction

//    val t = ByteBuffer.allocate(8)
//    t.putLong(0,0)
//    PCIEInterface.writeBytes(FilterPCIEInterface.HOSTIN_OFFSET * 8,t.array())
//    PCIEInterface.writeBytes(FilterPCIEInterface.HOSTOUT_OFFSET * 8,t.array())
//    PCIEInterface.writeBytes(FilterPCIEInterface.TSTATE_OFFSET * 8,t.array())

//    var ret = PCIEInterface.readBytes(FilterPCIEInterface.CLIENTOUT_OFFSET*8, 8)
//    ret = PCIEInterface.readBytes(FilterPCIEInterface.CLIENTIN_OFFSET*8, 8)
//    ret = PCIEInterface.readBytes(FilterPCIEInterface.STATEIN_OFFSET*8, 8)
//    ret = PCIEInterface.readBytes(FilterPCIEInterface.STATEOUT_OFFSET*8, 8)
//    ret = PCIEInterface.readBytes(FilterPCIEInterface.FSTATE_OFFSET*8, 8)
//    ret = PCIEInterface.readBytes(FilterPCIEInterface.PROBE_OFFSET*8, 8)
//    val k = ByteBuffer.wrap(ret).getLong
//    ret = PCIEInterface.readBytes(0*8, 8)


    val a = new Array[Long](1000)
    for (i <- 0 until 1000)
      a.update(i,1024 - i)
    val ret = FilterPCIEInterface.filter(a)
    ret.foreach(println(_))

    FilterPCIEInterface.endTransaction
  }
}