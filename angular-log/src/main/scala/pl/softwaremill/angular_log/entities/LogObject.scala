package pl.softwaremill.angular_log.entities

import java.util.Date
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
