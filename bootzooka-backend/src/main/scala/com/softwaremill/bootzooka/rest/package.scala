package com.softwaremill.bootzooka

import com.softwaremill.bootzooka.rest.swagger.BootzookaSwagger

package object rest {

  implicit val bootzookaSwagger = new BootzookaSwagger
}
