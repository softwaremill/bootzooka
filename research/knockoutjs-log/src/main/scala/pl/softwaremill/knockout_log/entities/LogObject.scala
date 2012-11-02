package pl.softwaremill.knockout_log.entities

import java.util.Date
import org.codehaus.jackson.annotate.JsonProperty

class LogObject(anId: Long, t: String, d: String, a: String) {

  @JsonProperty
  var id = anId;

  @JsonProperty
  var text = t;

  @JsonProperty
  var date = d;

  @JsonProperty
  var author = a;

  def this() = {
    this(0, null, null, null)
  }

  override def toString = {
    "[LogObject: id = " + id + ", text = " + text +", author = " + author +", date = " + date + "]";
  }
}
