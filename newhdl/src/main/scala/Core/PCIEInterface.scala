package Core

case class PCIEInterface(deviceID: String) {

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
