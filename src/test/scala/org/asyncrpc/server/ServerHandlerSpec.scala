package org.asyncrpc.server

import java.util.concurrent.ConcurrentHashMap

import io.netty.channel.embedded.EmbeddedChannel
import org.asyncrpc.integration.api.User
import org.asyncrpc.model.Request
import org.scalatest.FunSpec

class ServerHandlerSpec extends FunSpec {

  describe("ServerHandler") {
    it("should handle incoming request and send back response") {
      val user = new User(0L, "name")
      val services = new ConcurrentHashMap[String, AnyRef]()
      services.put(classOf[EchoService].getName, new EchoService {
        override def echo(message: String): String = message
      })
      val handler = new ServerHandler(services)
      val channel = new EmbeddedChannel(handler)
      channel.writeInbound(new Request(classOf[EchoService].getName, "echo", Array("hi")))
      assert(channel.finish())
      val result = channel.readOutbound().asInstanceOf[String]
      assert(result === "hi")
    }
  }

  trait EchoService {

    def echo(message: String) : String

  }

}
