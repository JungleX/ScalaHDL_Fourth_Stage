`timescale 1ns / 1ps
//////////////////////////////////////////////////////////////////////////////////
// Company: 
// Engineer: 
// 
// Create Date: 2018/05/29 20:10:49
// Design Name: 
// Module Name: quartic_func
// Project Name: 
// Target Devices: 
// Tool Versions: 
// Description: 
// 
// Dependencies: 
// 
// Revision:
// Revision 0.01 - File Created
// Additional Comments:
// 
//////////////////////////////////////////////////////////////////////////////////

module mux(
    input[63:0] a,b,c,d,e,
    input[3:0] sel,
    output reg [63:0] y
);
    
    always@*
    case (sel)
    //0: y = a;
    1: y = b;
    3: y = c;
    5: y = d;
    7: y = e;
    default : y = 64'h0000000000000000;
    endcase
endmodule
module quartic_func(
    input clk,invalid,
    input [63:0] x,a,b,c,d,e,
    output reg [63:0] res,
    output reg outvalid
    );
    reg [3:0] state;
    wire [63:0] mvalc,avalc,y;
    reg [63:0] mvala,mvalb,avala,avalb;
    reg delay;
    wire multv,addv;
    mux mu(a,b,c,d,e,state,y);
    floating_point_0 mult (
      .aclk(clk),                                  // input wire aclk
      .s_axis_a_tvalid(1),            // input wire s_axis_a_tvalid
      .s_axis_a_tdata(mvala),              // input wire [63 : 0] s_axis_a_tdata
      .s_axis_b_tvalid(1),            // input wire s_axis_b_tvalid
      .s_axis_b_tdata(mvalb),              // input wire [63 : 0] s_axis_b_tdata
      .m_axis_result_tvalid(multv),  // output wire m_axis_result_tvalid
      .m_axis_result_tdata(mvalc)    // output wire [63 : 0] m_axis_result_tdata
    );
    floating_point_1 add (
      .aclk(clk),                                  // input wire aclk
      .s_axis_a_tvalid(1),            // input wire s_axis_a_tvalid
      .s_axis_a_tdata(avala),              // input wire [63 : 0] s_axis_a_tdata
      .s_axis_b_tvalid(1),            // input wire s_axis_b_tvalid
      .s_axis_b_tdata(avalb),              // input wire [63 : 0] s_axis_b_tdata
      .m_axis_result_tvalid(addv),  // output wire m_axis_result_tvalid
      .m_axis_result_tdata(avalc)    // output wire [63 : 0] m_axis_result_tdata
    );
    always@(posedge clk) begin
        if (invalid) 
        begin
            state <= 4'b0000;   
            outvalid <= 1'b0;
            delay <= 0;
        end else 
        begin
            if (state != 4'b1000) 
            begin
                case (state[0])
                1'b0:
                    begin
                        if (delay) begin
                        mvala <= state == 4'b0000 ? a: avalc;
                        mvalb <= x;
                        state <= state + 1;
                        delay <= delay +1;
                        end 
                        else delay <= delay +1;
                    end
                1'b1:
                    begin
                        if (delay) begin
                        avala <= mvalc;
                        avalb <= y;
                        state <= state + 1;
                        delay <= delay +1;
                        end
                        else delay <= delay +1;
                    end
                endcase
            end else
            begin
                if (delay) begin
                res <= avalc;
                outvalid <= 1'b1; 
                delay <= delay +1;
                end
                else delay <= delay +1;
            end
        end
    end
endmodule
