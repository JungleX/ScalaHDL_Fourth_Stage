package QuarticFunction


import java.nio.ByteBuffer

import NewHDL.Core.PCIEInterface
object QFPCIEInterface {
  private var tid:Long = 0
  def startTransaction = {
    PCIEInterface.startTransaction
    tid = 0
  }
  def endTransaction = PCIEInterface.endTransaction
  def calc(a:Double,b:Double,c:Double,d:Double,e:Double,x:Double) ={
    val buf = ByteBuffer.allocate(56)
    val t = ByteBuffer.allocate(8)
    t.clear()
    t.putDouble(a)
    buf.put(t.array().reverse)
    t.clear()
    t.putDouble(b)
    buf.put(t.array().reverse)
    t.clear()
    t.putDouble(c)
    buf.put(t.array().reverse)
    t.clear()
    t.putDouble(d)
    buf.put(t.array().reverse)
    t.clear()
    t.putDouble(e)
    buf.put(t.array().reverse)
    t.clear()
    t.putDouble(x)
    buf.put(t.array().reverse)
    t.clear()
    t.putLong(tid)
    buf.put(t.array().reverse)
    tid = tid + 1
    PCIEInterface.writeBytes(0,buf.array())
    val res = PCIEInterface.readBytes(80,8)
    ByteBuffer.wrap(res.reverse).getDouble(0)
  }


}
//test

/*object Main {
  def main(args: Array[String]): Unit = {
    QFPCIEInterface.startTransaction
    val a = QFPCIEInterface.calc(1,2,3,4,5,2)
    println(a)
    QFPCIEInterface.endTransaction
  }
}*/