package org.asyncrpc.model

case class Request(val service: String, val method: String, val parameters: Array[Any]) extends java.io.Serializable {

}
