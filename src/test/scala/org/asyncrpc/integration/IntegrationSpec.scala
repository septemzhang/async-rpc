package org.asyncrpc.integration

import org.asyncrpc.client.Client
import org.asyncrpc.integration.api.impl.UserService
import org.asyncrpc.integration.api.{User, UserClient}
import org.asyncrpc.server.Server
import org.scalatest.{BeforeAndAfterAll, FunSpec}

import scala.concurrent.{Future, Await}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class IntegrationSpec extends FunSpec with BeforeAndAfterAll {

  val port = 9007
  val userService = new UserService
  val server = new Server
  server.serve(userService)
  server.bind(port)

  val client = new Client("127.0.0.1", port)

  override def afterAll(): Unit = {
    server.close()
    client.close
  }

  describe("UserService") {
    it("should update users with even id") {
      val futureOfUsers = (
        for {
          users <- client.call { userClient: UserClient => userClient.getAll() }
          updatedUsers <- updateAll(users)
        } yield updatedUsers
      ) recover {
        case e: Throwable =>
          e.printStackTrace()
          List[User]()
      }

      val users = Await.result(futureOfUsers, 1.seconds)
      assert(users.size === 1)
      assert(users.head.id === 0)
      assert(users.head.name === "newName")
    }
  }

  def updateAll(users: List[User]) : Future[List[User]] = {
    val futures = users filter { user => user.id % 2 == 0 } map { user =>
        client.call { userClient: UserClient => userClient.update(user.id, "newName") } map (_.get)
    }
    Future.sequence(futures)
  }

}
