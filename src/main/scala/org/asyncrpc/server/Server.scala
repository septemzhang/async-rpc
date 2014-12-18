package org.asyncrpc.server

import java.util.concurrent.ConcurrentHashMap

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel._
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.serialization.{ClassResolvers, ObjectDecoder, ObjectEncoder}
import io.netty.handler.logging.{LogLevel, LoggingHandler}

class Server {

  private val services = new ConcurrentHashMap[String, AnyRef]()
  private val bossGroup = new NioEventLoopGroup()
  private val workerGroup = new NioEventLoopGroup()
  private val handler = new ServerHandler(services)
  private val b = bootstrap

  private def bootstrap : ServerBootstrap = {
    new ServerBootstrap().group(bossGroup, workerGroup)
      .channel(classOf[NioServerSocketChannel])
      //      .option(ChannelOption.SO_BACKLOG, 100)
      .option[java.lang.Integer](ChannelOption.SO_BACKLOG, 100)
      .handler(new LoggingHandler(LogLevel.INFO))
      .childHandler(new ChannelInitializer[SocketChannel]() {
        override def initChannel(ch: SocketChannel) {
          ch.pipeline().addLast(
            new ObjectEncoder(), new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
            handler
          )
        }
    })
  }

  //TODO what if the given service implements multiple interfaces?
  def serve(service: AnyRef) = {
    val name = findInterface(service)
    services.put(name, service)
    this
  }

  private def findInterface(service: AnyRef) : String = {
    val interfaces = service.getClass.getInterfaces
    if (interfaces.length == 0) {
      throw new RuntimeException(s"service: ${service.getClass.getSimpleName} doesn't implement any interface")
    }
    interfaces(0).getName
  }

  def bind(port: Int) = {
    val f = b.bind(port).sync()
    f.channel().closeFuture()
  }

  def close() {
    bossGroup.shutdownGracefully()
    workerGroup.shutdownGracefully()
  }

}