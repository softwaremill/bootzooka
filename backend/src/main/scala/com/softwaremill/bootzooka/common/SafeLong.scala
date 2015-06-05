package com.softwaremill.bootzooka.common

object SafeLong {
  val LongPattern = "-?([0-9]+)"

  def apply(o: Option[String]): Option[Long] = apply(o.getOrElse(""))

  def apply(o: String): Option[Long] = if (o.matches(LongPattern)) Some(o.toLong) else None

  def unapply(o: String): Option[Long] = if (o.matches(LongPattern)) Some(o.toLong) else None

  def orNone(i: Any): Option[Long] = i match {
    case i: Int => Some(i.toLong)
    case l: Long => Some(l)
    case _ => None
  }
}