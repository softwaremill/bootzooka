package pl.softwaremill.knockout_log.services

import org.codehaus.jackson.annotate.JsonProperty

class LongResponseWrapper(x: Long) {

  @JsonProperty
  def value = x;

}
