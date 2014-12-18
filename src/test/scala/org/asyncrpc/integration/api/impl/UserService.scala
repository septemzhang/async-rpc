package org.asyncrpc.integration.api.impl

import org.asyncrpc.integration.api.{User, UserClient}

class UserService extends UserClient {

  val users = List(new User(0L, "0"), new User(1L, "1"))

  override def get(id: Long): Option[User] = users.find(_.id == id)

  override def getAll(): List[User] = users

  override def update(id: Long, newName: String): Option[User] = {
    get(id) map { user => user.copy(name = newName) }
  }

}
