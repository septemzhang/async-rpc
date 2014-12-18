package org.asyncrpc.server

import java.util.concurrent.ConcurrentHashMap

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import org.asyncrpc.model.Request

@Sharable
class ServerHandler(services : ConcurrentHashMap[String, AnyRef]) extends SimpleChannelInboundHandler[Request] {

  override def channelRead0(ctx: ChannelHandlerContext, request: Request) {
    val result = handle(request)
    ctx.write(result)
  }

  override def channelReadComplete(ctx: ChannelHandlerContext) {
    ctx.flush()
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    cause.printStackTrace()
    ctx.close()
  }

  private def handle(request: Request) : AnyRef = {
    val service = services.get(request.service)
    val methods = service.getClass.getDeclaredMethods
    //TODO should check on type of parameters to differentiate overridden methods
    methods.find(_.getName == request.method) match {
      case Some(m) => m.invoke(service, request.parameters.asInstanceOf[Array[Object]]: _*)
      case None => throw new RuntimeException(s"method: ${request.method} not found for service: ${request.service}")
    }
 }

}
