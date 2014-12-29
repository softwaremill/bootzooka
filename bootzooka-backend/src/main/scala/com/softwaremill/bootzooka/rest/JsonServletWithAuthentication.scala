package com.softwaremill.bootzooka.rest

import org.json4s.{ DefaultFormats, Formats }
import com.softwaremill.bootzooka.auth.RememberMeSupport

abstract class JsonServletWithAuthentication extends JsonServlet with RememberMeSupport {

}