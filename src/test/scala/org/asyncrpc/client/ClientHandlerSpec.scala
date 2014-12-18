package org.asyncrpc.client

import java.util.concurrent.TimeUnit

import io.netty.channel.embedded.EmbeddedChannel
import org.asyncrpc.integration.api.User
import org.scalatest.FunSpec

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Promise}

class ClientHandlerSpec extends FunSpec {

  describe("ClientHandler") {
    it("should complete the promise when response received") {
      val user = new User(0L, "name")
      val promise = Promise[User]
      val handler = new ClientHandler(promise.asInstanceOf[Promise[Any]])
      val channel = new EmbeddedChannel(handler)
      channel.writeInbound(user)
      assert(channel.finish() === false)
      assert(promise.isCompleted)
      assert(channel.isOpen === false)
      assert(Await.result(promise.future, Duration(10, TimeUnit.MILLISECONDS)) === user)
    }
  }

}
