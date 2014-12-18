package org.asyncrpc.integration.api

trait UserClient {

  def get(id: Long) : Option[User]

  def getAll() : List[User]

  def update(id: Long, newName: String) : Option[User]

}
