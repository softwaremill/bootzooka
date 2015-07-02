package com.softwaremill.bootzooka.api

import com.softwaremill.bootzooka.auth.RememberMeSupport

abstract class JsonServletWithAuthentication extends JsonServlet with RememberMeSupport {

}