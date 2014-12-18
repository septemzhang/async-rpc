package org.asyncrpc.client

import io.netty.channel.{ChannelHandlerContext, ChannelInboundHandlerAdapter}

import scala.concurrent.Promise

class ClientHandler(val promise: Promise[Any]) extends ChannelInboundHandlerAdapter {

   override def channelRead(ctx: ChannelHandlerContext, msg: AnyRef) {
     promise.success(msg)
     ctx.close()
   }

   override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
     cause.printStackTrace()
     ctx.close()
   }

 }
