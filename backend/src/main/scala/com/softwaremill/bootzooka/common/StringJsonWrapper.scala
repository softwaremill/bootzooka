package com.softwaremill.bootzooka.common

/**
 * This class wraps string, so when this value is serialized as json not as plain string
 * e.g.
 * {"value":"Some text"} not "Some text"
 */
case class StringJsonWrapper(value: String)
