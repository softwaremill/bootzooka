package com.softwaremill.bootzooka

import com.softwaremill.bootzooka.rest.swagger.AppSwagger

package object rest {

  implicit val bootzookaSwagger = new AppSwagger
}
