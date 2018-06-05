package NewHDL.Core

import java.io.{FileInputStream, FileOutputStream}
import java.nio.ByteBuffer

object PCIEInterface {
  private val dmaInNmae = "/dev/xdma0_c2h_0"
  private val dmaOutName = "/dev/xdma0_h2c_0"

  private var is:FileInputStream = null

  private var os:FileOutputStream = null
  def startTransaction = {
    is = new FileInputStream(dmaInNmae)
    os = new FileOutputStream(dmaOutName)

  }
  def endTransaction = {
    is.close()
    os.close()
  }
  def readBytes(addr:Int,size:Int): Array[Byte] = {
    val buffer = ByteBuffer.allocate(size)

    is.getChannel.read(buffer,addr)
    buffer.array()
  }
  def readByteBuffer(addr:Int,size:Int): ByteBuffer ={
    val buffer = ByteBuffer.allocate(size)

    is.getChannel.read(buffer,addr)
    buffer
  }
  def writeBytes(addr:Int, buffer:Array[Byte]): Unit = {
    os.getChannel.write(ByteBuffer.wrap(buffer),addr)
  }
  def writeBytes(addr:Int, byteBuffer:ByteBuffer) ={
    os.getChannel.write(byteBuffer,addr)
  }

}





// test draft




/*
object Main{
  def main(args: Array[String]): Unit = {
    PCIEInterface.startTransaction
    //val bytes = new Array[Byte](1024)
    //for (i <- 0 to 1023) bytes(i) = i.toByte
    //PCIEInterface.writeBytes(0,bytes)
    val bb = ByteBuffer.allocate(56)
    val bt = ByteBuffer.allocate(8)
    bt.clear()
    bt.putDouble(1.0)
    bb.put(bt.array().reverse)
    //println(bb.getDouble(0))
    //PCIEInterface.writeBytes(0,bb.array().reverse)
    bt.clear()
    bt.putDouble(2.0)
    bb.put(bt.array().reverse)
    bt.clear()
    bt.putDouble(3.0)
    bb.put(bt.array().reverse)
    bt.clear()
    bt.putDouble(4.0)
    bb.put(bt.array().reverse)
    bt.clear()
    bt.putDouble(5.0)
    bb.put(bt.array().reverse)
    bt.clear()
    bt.putDouble(-1.0)
    bb.put(bt.array().reverse)
    bt.clear()
    bt.putLong(5)
    bb.put(bt.array().reverse)
    //println(bb.getDouble(0))
    PCIEInterface.writeBytes(0,bb.array())

    //PCIEInterface.writeBytes(24,Array[Byte](1,0,0,0,0,0,0,0))
    //var rbytes = PCIEInterface.readBytes(32,8)
    /*while (rbytes(0) != 1) {
      rbytes = PCIEInterface.readBytes(32,8)
      println(rbytes(0))
    }*/
    val b:ByteBuffer = PCIEInterface.readByteBuffer(80,8)
    println(ByteBuffer.wrap(b.array().reverse).getDouble(0))

    //b.array().foreach(printf("%h\n",_))
    //println(b.getDouble(0))
    //println(b.getLong(0))
    //b.rewind()
    //for (i <- 0 to 10) println(b.getDouble())
    //PCIEInterface.endTransaction
  }
} */
//  val buf1 = Array[Byte](1,2,3,4,5,6,7,8)
//  val buf2:Array[Byte] = new Array[Byte](16)
//  val buf3:ByteBuffer = ByteBuffer.allocate(24)


  /*def main(args: Array[String]): Unit ={
    val is = new FileInputStream(dmaInNmae)
    val os = new FileOutputStream(dmaOutName)
    //os.write(buf1,0,8)
    os.getChannel.position(8).write(ByteBuffer.wrap(buf1,0,8))
    os.getChannel.position(0).write(ByteBuffer.wrap(buf1,0,8))

    is.getChannel.read(buf3)
    println(buf3.hasArray)




    is.close()
    os.close()
    //println(buf3.asCharBuffer().array().length)

    for (b <- buf3.array()) println(b)
  }
*/



/*case class PCIEInterface(deviceID: String) {

  System.loadLibrary("PCIEDriver")

   @native def write_from_buffer(oname:String, buffer:Array[Byte], size: Int ): Unit

   @native def read_to_buffer(fnmae:String, size:Int, base: Int):Array[Byte]

  def writeToFPGA(buffer: Array[Byte]): Unit ={
    write_from_buffer(deviceID, buffer, buffer.length)
  }
  def readFromFPGA(size:Int, base:Int): Array[Byte] ={
    read_to_buffer(deviceID, size, base)
  }


}
*/