package Filter

import NewHDL.Core.HDLBase._

class  Filter(clk:HDL[Boolean],
              reset:HDL[Boolean],
              we:HDL[Boolean],
              re:HDL[Boolean],
              datain:HDL[Unsigned],
              addr:HDL[Unsigned],
              dataout:HDL[Unsigned]) extends HDLClass {
  def filter = module{
    val BUFFER_SIZE = 4096
    val state = HDLlize(Unsigned(0,2))
    val dout = HDLlize(Unsigned(0,128))
    val usrreset = HDLlize(Unsigned(0,128))
    val index = HDLlize(Unsigned(0,13))
    val offset = HDLlize(Unsigned(0,13))

    val rdatain1 = HDLlize(Unsigned(0,64))
    val rdatain2 = HDLlize(Unsigned(0,64))
    val wdout = HDLlize(Unsigned(0,128))
    val offset2 = HDLlize(Unsigned(0,13))

    val f = HDLlize(b0)
    val temp = HDLlize(Unsigned(0,64))
    val waddr = HDLlize(Unsigned(0,13))
    val memaddr = HDLlize(Unsigned(0,11))

    async{
      rdatain1 := datain(0,64)
      rdatain2 := datain(64,128)
      offset2 := BUFFER_SIZE - offset

      when(addr > (BUFFER_SIZE >> 1) ){
        dataout := dout(0,128)
      }.otherwise{
        dataout := wdout(0,128)
      }

      when(re){
        when(state(0) is 1){
          memaddr := offset2 + addr * 2
        }.otherwise{
          memaddr := offset + addr * 2
        }
      }.otherwise{
        memaddr := waddr
      }
    }

    instance("blk_mem_gen_0",
      "ram",
      clk,
      f,
      memaddr,
      temp,
      wdout
    )

    sync(clk,1){
      when(reset is 0){
        state := 0
        offset := 0
        index := 0
      }.otherwise{
        when(we){
          switch(addr)
            .is(4095){
              index := 0
              offset := 0
              state := 0
              f := 0
              temp := 0
            }
            .is(4094){
              state := 0
            }
            .is(4093){
              f := 0
            }
            .default{
              when(rdatain1 < rdatain2){
                temp := rdatain2
                f := 1
                waddr := index + offset
                when(index is  BUFFER_SIZE-1){
                  index :=0
                  state := 1
                  offset := offset2
                }.otherwise {
                  index := index + 1
                }
              }.otherwise {
                f :=0
              }
            }
        }
      }
    }

    sync(clk,1){
      when(reset is 0){

      }.otherwise{
        when(re){
          switch(addr)
            .is(4094){
              dout := state
            }
            .is(4093){
              dout := index
            }
        }
      }
    }
  }

  override val toCompile = List(filter)
}

//object Main {
//  def main(args: Array[String]){
//    println(new Filter(b0,b0,b0,b0,Unsigned(0,128),Unsigned(0,13),
//      Unsigned(0,128)).compile)
//  }
//}