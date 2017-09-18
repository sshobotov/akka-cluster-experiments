package labs.troy
package model

abstract case class SessionData(shopToken: String) {
  def sizeInBytes: Long
}
