package pl.softwaremill.demo.scalate.entities

import org.codehaus.jackson.annotate.JsonProperty

class LogObject(t: String, d: String, a: String) {

  @JsonProperty
  val text = t;

  @JsonProperty
  val date = d;

  @JsonProperty
  val author = a;

  def this() = {
    this(null, null, null)
  }

  override def toString = {
    "[LogObject: text = " + text +", author = " + author +", date = " + date + "]";
  }

}
