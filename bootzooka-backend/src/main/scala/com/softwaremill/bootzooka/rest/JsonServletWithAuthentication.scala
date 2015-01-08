package com.softwaremill.bootzooka.rest

import com.softwaremill.bootzooka.auth.RememberMeSupport

abstract class JsonServletWithAuthentication extends JsonServlet with RememberMeSupport {

}