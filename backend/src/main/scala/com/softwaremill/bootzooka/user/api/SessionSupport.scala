/*
 * COPYRIGHT (c) 2016 VOCADO, LLC.  ALL RIGHTS RESERVED.  THIS SOFTWARE CONTAINS
 * TRADE SECRETS AND/OR CONFIDENTIAL INFORMATION PROPRIETARY TO VOCADO, LLC AND/OR
 * ITS LICENSORS. ACCESS TO AND USE OF THIS INFORMATION IS STRICTLY LIMITED AND
 * CONTROLLED BY VOCADO, LLC.  THIS SOFTWARE MAY NOT BE COPIED, MODIFIED, DISTRIBUTED,
 * DISPLAYED, DISCLOSED OR USED IN ANY WAY NOT EXPRESSLY AUTHORIZED BY VOCADO, LLC IN WRITING.
 */

package com.softwaremill.bootzooka.user.api

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.softwaremill.bootzooka.user.application.{Session, UserService}
import com.softwaremill.bootzooka.user.domain.BasicUserData
import com.softwaremill.bootzooka.user.UserId
import com.softwaremill.session.SessionDirectives._
import com.softwaremill.session.SessionOptions._
import com.softwaremill.session.{RefreshTokenStorage, SessionManager}

import scala.concurrent.ExecutionContext

trait SessionSupport {

  implicit def sessionManager: SessionManager[Session]

  implicit def refreshTokenStorage: RefreshTokenStorage[Session]

  implicit def ec: ExecutionContext

  def userService: UserService

  def userFromSession: Directive1[BasicUserData] = userIdFromSession.flatMap { userId =>
    onSuccess(userService.findById(userId)).flatMap {
      case None => reject(AuthorizationFailedRejection)
      case Some(user) => provide(user)
    }
  }

  def userIdFromSession: Directive1[UserId] = session(refreshable, usingCookies).flatMap {
    _.toOption match {
      case None => reject(AuthorizationFailedRejection)
      case Some(s) => provide(s.userId)
    }
  }
}
