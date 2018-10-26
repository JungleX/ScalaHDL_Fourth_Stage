package Filter

import java.nio.{ByteBuffer, LongBuffer}

import scala.collection.immutable.Vector
import NewHDL.Core.PCIEInterface

object FilterPCIEInterface {

//  val HOSTIN_OFFSET = 512
//  val HOSTOUT_OFFSET = 514
//  val TSTATE_OFFSET = 768
//  val CLIENTIN_OFFSET = 515
//  val CLIENTOUT_OFFSET = 513
//  val STATEIN_OFFSET = 516
//  val STATEOUT_OFFSET = 517
//  val FSTATE_OFFSET = 518
//  val PROBE_OFFSET = 519
//  val IDXIN_OFFSET = 520
//  val IDXOUT_OFFSET = 521

  val RESET_OFFSET = 4095
  val STATE_OFFSET = 4094
  val IDX_OFFSET = 4093
  val FINISH_OFFSET = 4093
  //val CHUNK_SIZE = 4096
  def startTransaction = {
    PCIEInterface.startTransaction

  }
  def endTransaction = PCIEInterface.endTransaction

  def cpufilter(a:Array[Byte]) :Array[Byte] ={
    val ab = ByteBuffer.wrap(a).asLongBuffer()
    var ans = Vector[Byte]()
    for (i <- 0 until ab.capacity()/2){
      if (ab.get(2*i)  < ab.get(2*i + 1)) {
        ans = ans ++ a.slice(16*i,16*i+8)
      }
    }
    ans.toArray
  }


  def fpgafilter3(a:Array[Byte],CHUNK_SIZE:Int) : Array[Byte] ={
    val buf = ByteBuffer.allocate(8*CHUNK_SIZE)

    var res = Vector[Byte]()
    val t = ByteBuffer.allocate(16)
    val n = (a.length - 1) / (8*CHUNK_SIZE) + 1

    //init
    t.putLong(0,0)
    PCIEInterface.writeBytes(RESET_OFFSET * 16,t.array())


    var ret = new Array[Byte](0)
    var state:Long = 0
    var idx:Long = 0

    for (i <- 0 until n) {
      buf.clear()

      buf.put(a.slice(i*8*CHUNK_SIZE,(i+1)*8*CHUNK_SIZE))

      PCIEInterface.writeBytes(0,buf.array())
      PCIEInterface.writeBytes(FINISH_OFFSET*16,t.array())
      ret = PCIEInterface.readBytes(STATE_OFFSET * 16,16)
      state = ByteBuffer.wrap(ret).getLong()

      if (state == 1) {
        ret = PCIEInterface.readBytes(0,CHUNK_SIZE * 8)
        res = res ++ ret
        PCIEInterface.writeBytes(STATE_OFFSET * 16, t.array())
      }
    }
    ret = PCIEInterface.readBytes(IDX_OFFSET * 16,16)
    idx = ByteBuffer.wrap(ret).getLong()
    if (idx != 0) {
      ret = PCIEInterface.readBytes(0,(idx * 8).toInt)
      res = res ++ ret
    }







    res.toArray
  }


}
//test

object Main {
  def main(args: Array[String]): Unit = {
    FilterPCIEInterface.startTransaction


    val a = new Array[Long](4096)

    for (i <- 0 until 4096)
      a.update(i, i)
    val ba = a.flatMap(ByteBuffer.allocate(8).putLong(_).array())

    println("Test Setup: 32KB DATA, 2000 Iteration.")
    println("=======================================")
    println("CPU (3.3GHz * 4) Start:")
    print("Time Elaspe: ")
    var t1 = System.currentTimeMillis()
    for (i <- 0 until 2000) {
      val ret = FilterPCIEInterface.cpufilter(ba)
    }
    var t2 = System.currentTimeMillis()
    val base = (t2-t1) * 1.0
    printf("%fms\n",base)



    println("=======================================")
    println("FPGA (250MHz, Chunck Size 4KB) Start:")
    print("Time Elaspe: ")
    t1 = System.currentTimeMillis()
    for (i <- 0 until 2000) {
     val ret = FilterPCIEInterface.fpgafilter3(ba,512)

    }
    t2 = System.currentTimeMillis()
    printf("%fms\n",(t2 -t1)*1.0)
    printf("Speedup: %f%%\n",base/(t2-t1)*100)

    println("=======================================")
    println("FPGA (250MHz, Chunck Size 8KB) Start:")
    print("Time Elaspe: ")
    t1 = System.currentTimeMillis()
    for (i <- 0 until 2000) {
      val ret = FilterPCIEInterface.fpgafilter3(ba,1024)

    }
    t2 = System.currentTimeMillis()
    printf("%fms\n",(t2 -t1)*1.0)
    printf("Speedup: %f%%\n",base/(t2-t1)*100)

    println("=======================================")
    println("FPGA (250MHz, Chunck Size 16KB) Start:")
    print("Time Elaspe: ")
    t1 = System.currentTimeMillis()
    for (i <- 0 until 2000) {
      val ret = FilterPCIEInterface.fpgafilter3(ba,2048)

    }
    t2 = System.currentTimeMillis()
    printf("%fms\n",(t2 -t1)*1.0)
    printf("Speedup: %f%%\n",base/(t2-t1)*100)

    println("=======================================")
    println("FPGA (250MHz, Chunck Size 32KB) Start:")
    print("Time Elaspe: ")
    t1 = System.currentTimeMillis()
    for (i <- 0 until 2000) {
      val ret = FilterPCIEInterface.fpgafilter3(ba,4096)

    }
    t2 = System.currentTimeMillis()
    printf("%fms\n",(t2 -t1)*1.0)
    printf("Speedup: %f%%\n",base/(t2-t1)*100)

    FilterPCIEInterface.endTransaction

}
//    val t = ByteBuffer.allocate(8)
//    t.putLong(0,13)
//    PCIEInterface.writeBytes(8,t.array())
//    val ret = PCIEInterface.readBytes(8, 8)
//    val temp = ByteBuffer.wrap(ret).getLong
//    PCIEInterface.writeBytes(FilterPCIEInterface.HOSTOUT_OFFSET * 8,t.array())
//    PCIEInterface.writeBytes(FilterPCIEInterface.TSTATE_OFFSET * 8,t.array())

//   var ret = PCIEInterface.readBytes(1020*16, 16)
//    ret = PCIEInterface.readBytes(32, 16)
//    ret = PCIEInterface.readBytes(FilterPCIEInterface.CLIENTIN_OFFSET*8, 8)
//    ret = PCIEInterface.readBytes(FilterPCIEInterface.STATEIN_OFFSET*8, 8)
//    ret = PCIEInterface.readBytes(FilterPCIEInterface.STATEOUT_OFFSET*8, 8)
//    ret = PCIEInterface.readBytes(FilterPCIEInterface.FSTATE_OFFSET*8, 8)
//    ret = PCIEInterface.readBytes(FilterPCIEInterface.PROBE_OFFSET*8, 8)
//    val k = ByteBuffer.wrap(ret).getLong
//    ret = PCIEInterface.readBytes(0*8, 8)

//ret.foreach(println(_))
//    var r = PCIEInterface.readBytes(FilterPCIEInterface.CLIENTOUT_OFFSET*8, 8)
//    println(ByteBuffer.wrap(r).getLong())
//    r = PCIEInterface.readBytes(FilterPCIEInterface.CLIENTIN_OFFSET*8, 8)
//    println(ByteBuffer.wrap(r).getLong())
//    r = PCIEInterface.readBytes(FilterPCIEInterface.STATEIN_OFFSET*8, 8)
//    println(ByteBuffer.wrap(r).getLong())
//    r = PCIEInterface.readBytes(FilterPCIEInterface.STATEOUT_OFFSET*8, 8)
//    println(ByteBuffer.wrap(r).getLong())
//    r = PCIEInterface.readBytes(FilterPCIEInterface.FSTATE_OFFSET*8, 8)
//    println(ByteBuffer.wrap(r).getLong())
//    r = PCIEInterface.readBytes(FilterPCIEInterface.PROBE_OFFSET*8, 8)
//    println(ByteBuffer.wrap(r).getLong())
  //      //      val b = ByteBuffer.wrap(ret).asLongBuffer()
  //      //      for (l <- 0 until b.capacity())
  //      //        println(b.get(l))
}
//            val b = ByteBuffer.wrap(ret).asLongBuffer()
//            for (l <- 0 until b.capacity())
//              println(b.get(l))
//    for (i <- 0 until 1) {
//      val ret = FilterPCIEInterface.filter(a)
//      for (l <- ret)
//        println(l)
//    }