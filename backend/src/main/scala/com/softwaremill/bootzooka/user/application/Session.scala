/*
 * COPYRIGHT (c) 2016 VOCADO, LLC.  ALL RIGHTS RESERVED.  THIS SOFTWARE CONTAINS
 * TRADE SECRETS AND/OR CONFIDENTIAL INFORMATION PROPRIETARY TO VOCADO, LLC AND/OR
 * ITS LICENSORS. ACCESS TO AND USE OF THIS INFORMATION IS STRICTLY LIMITED AND
 * CONTROLLED BY VOCADO, LLC.  THIS SOFTWARE MAY NOT BE COPIED, MODIFIED, DISTRIBUTED,
 * DISPLAYED, DISCLOSED OR USED IN ANY WAY NOT EXPRESSLY AUTHORIZED BY VOCADO, LLC IN WRITING.
 */

package com.softwaremill.bootzooka.user.application

import java.util.UUID

import com.softwaremill.bootzooka.user._
import com.softwaremill.session.{MultiValueSessionSerializer, SessionSerializer}

import scala.util.Try

case class Session(userId: UserId)

object Session {
  implicit val serializer: SessionSerializer[Session, String] = new MultiValueSessionSerializer[Session](
    (t: Session) => Map("id" -> t.userId.toString),
    m => Try { Session(UUID.fromString(m("id"))) }
  )
}
