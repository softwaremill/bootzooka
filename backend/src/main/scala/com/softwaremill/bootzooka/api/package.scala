package com.softwaremill.bootzooka

import com.softwaremill.bootzooka.api.swagger.AppSwagger

package object api {

  implicit val bootzookaSwagger = new AppSwagger
}
