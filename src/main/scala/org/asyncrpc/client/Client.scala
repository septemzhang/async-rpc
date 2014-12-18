package org.asyncrpc.client

import java.lang.reflect.{Method, InvocationHandler, Proxy}

import io.netty.bootstrap.Bootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.channel.{Channel, ChannelInitializer, ChannelOption}
import io.netty.handler.codec.serialization.{ClassResolvers, ObjectDecoder, ObjectEncoder}
import org.asyncrpc.model.Request

import scala.concurrent._

class Client(host: String, port: Int) {

  private val group = new NioEventLoopGroup()

  private def connect(promise: Promise[Any]) : Channel = {
    val b = new Bootstrap()
    b.group(group).channel(classOf[NioSocketChannel])
      //      .option(ChannelOption.TCP_NODELAY, true)
      .option[java.lang.Boolean](ChannelOption.TCP_NODELAY, true)
      .handler(new ChannelInitializer[SocketChannel]() {

      override def initChannel(ch: SocketChannel) {
        val p = ch.pipeline()
        p.addLast(
          new ObjectEncoder(), new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
          new ClientHandler(promise)
        )
      }
    })
    val f = b.connect(host, port).sync()
    f.channel()
  }

  def call[T <: AnyRef, R](f: T => R)(implicit m: Manifest[T]): Future[R] = {
    val promise = Promise[Any]
    val channel = connect(promise)
    val p = proxy(m.runtimeClass.asInstanceOf[Class[T]], channel)
    f(p)
    promise.future.asInstanceOf[Future[R]]
  }

  private def proxy[T](service: Class[T], channel: Channel) : T = {
    if (!service.isInterface) throw new RuntimeException("interface required")
    Proxy.newProxyInstance(service.getClassLoader, Array(service), new InvocationHandler {
      override def invoke(proxy: scala.Any, method: Method, args: Array[AnyRef]): AnyRef = {
        //TODO filter out special method from java.lang.Object, such as toString ...
        val request = new Request(service.getName, method.getName, args.asInstanceOf[Array[Any]])
        channel.writeAndFlush(request)
        //fake result
        null
      }
    }).asInstanceOf[T]
  }

  def close: Unit = {
    group.shutdownGracefully()
  }

}
