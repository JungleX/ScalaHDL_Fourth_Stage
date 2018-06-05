package QuarticFunction

import NewHDL.Core.HDLBase._
class QuarticFunction(clk:HDL[Boolean],
                      invalid:HDL[Boolean],
                      x:HDL[Unsigned],
                      a:HDL[Unsigned],
                      b:HDL[Unsigned],
                      c:HDL[Unsigned],
                      d:HDL[Unsigned],
                      e:HDL[Unsigned],
                      res:HDL[Unsigned],
                      outvalid:HDL[Boolean]) extends HDLClass {

  /*def mux(a:HDL[Unsigned],
          b:HDL[Unsigned],
          c:HDL[Unsigned],
          d:HDL[Unsigned],
          e:HDL[Unsigned],
          sel:HDL[Unsigned],
          y:HDL[Unsigned]) = {
    async {
      switch(sel)
        .is(1) { y ::= b}
        .is(3) { y ::= c}
        .is(5) { y ::= d}
        .is(7) { y ::= e}
        .default { y ::= Unsigned(0,64)}
    }
  }*/

  def quartic_function(clk:HDL[Boolean],
                       invalid:HDL[Boolean],
                       x:HDL[Unsigned],
                       a:HDL[Unsigned],
                       b:HDL[Unsigned],
                       c:HDL[Unsigned],
                       d:HDL[Unsigned],
                       e:HDL[Unsigned],
                       res:HDL[Unsigned],
                       outvalid:HDL[Boolean]) ={

    val state = HDLlize(Unsigned(0,4))
    val mvalc = HDLlize(Unsigned(0,64))
    val avalc = HDLlize(Unsigned(0,64))
    val y = HDLlize(Unsigned(0,64))
    val mvala = HDLlize(Unsigned(0,64))
    val mvalb = HDLlize(Unsigned(0,64))
    val avala = HDLlize(Unsigned(0,64))
    val avalb = HDLlize(Unsigned(0,64))
    val delay = HDLlize(b0)
    val multv = HDLlize(b0)
    val addv = HDLlize(b0)
    instance("mux",
      "mu",
      a,
      b,
      c,
      d,
      e,
      state,
      y
    )
    instance("floating_point_0",
      "mult",
      clk,
      1,
      mvala,
      1,
      mvalb,
      multv,
      mvalc
    )
    instance("floating_point_1",
      "add",
      clk,
      1,
      avala,
      1,
      avalb,
      addv,
      avalc
    )

    sync(clk,1) {
      when(invalid){
        state := 0
        outvalid := 0
        delay := 0
      }.elsewhen(state isnot 8){
        switch(state(0))
          .is(0){
            when(delay){
              when(state is 0) {
                mvala := a
              }.otherwise {
                mvala := avalc
              }
              mvalb := x
              state := state + 1
              delay := delay + 1
            }.otherwise {
              delay := delay + 1
            }
          }
          .default{
            when (delay) {
              avala := mvalc
              avalb := y
              state := state + 1
              delay := delay +1
            }.otherwise {
              delay := delay + 1
            }

          }
      }.otherwise {
        when(delay) {
          res := avalc
          outvalid := 1
          delay := delay + 1
        }.otherwise{
          delay := delay + 1
        }
      }
    }
  }
  def m = module{
    quartic_function(clk,invalid,x,a,b,c,d,e,res,outvalid)
  }
  override val toCompile = List(m)
}

class mux(a:HDL[Unsigned],
          b:HDL[Unsigned],
          c:HDL[Unsigned],
          d:HDL[Unsigned],
          e:HDL[Unsigned],
          sel:HDL[Unsigned],
          y:HDL[Unsigned]) extends HDLClass {
  def m = module {
    async {
    switch(sel)
      .is(1) { y ::= b}
      .is(3) { y ::= c}
      .is(5) { y ::= d}
      .is(7) { y ::= e}
      .default { y ::= Unsigned(0,64)}
    }
  }

  override val toCompile: List[HDLModule] = List(m)
}
object Main {
  def main(args: Array[String]): Unit = {
    println(new QuarticFunction(b0,b0,Unsigned(0,64),Unsigned(0,64)
      ,Unsigned(0,64),Unsigned(0,64),Unsigned(0,64),Unsigned(0,64),Unsigned(0,64),
      b0).compile)
    println(new mux(Unsigned(0,64),Unsigned(0,64),Unsigned(0,64),Unsigned(0,64),
      Unsigned(0,64),Unsigned(0,4),Unsigned(0,64)).compile)
  }
}