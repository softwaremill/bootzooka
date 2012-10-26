package pl.softwaremill.angular_log.entities

import java.util.Date
import org.codehaus.jackson.annotate.JsonProperty

class LogObject(anId: Long, t: String, d: String, a: String) {

  @JsonProperty
  val id = anId;

  @JsonProperty
  val text = t;

  @JsonProperty
  val date = d;

  @JsonProperty
  val author = a;

  def this() = {
    this(0, null, null, null)
  }

  override def toString = {
    "[LogObject: id = " + id + ", text = " + text +", author = " + author +", date = " + date + "]";
  }
}
