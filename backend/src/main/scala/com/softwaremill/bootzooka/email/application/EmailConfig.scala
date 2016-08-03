/*
 * COPYRIGHT (c) 2016 VOCADO, LLC.  ALL RIGHTS RESERVED.  THIS SOFTWARE CONTAINS
 * TRADE SECRETS AND/OR CONFIDENTIAL INFORMATION PROPRIETARY TO VOCADO, LLC AND/OR
 * ITS LICENSORS. ACCESS TO AND USE OF THIS INFORMATION IS STRICTLY LIMITED AND
 * CONTROLLED BY VOCADO, LLC.  THIS SOFTWARE MAY NOT BE COPIED, MODIFIED, DISTRIBUTED,
 * DISPLAYED, DISCLOSED OR USED IN ANY WAY NOT EXPRESSLY AUTHORIZED BY VOCADO, LLC IN WRITING.
 */

package com.softwaremill.bootzooka.email.application

import com.typesafe.config.Config

trait EmailConfig {
  def rootConfig: Config

  private lazy val emailConfig = rootConfig.getConfig("email")

  lazy val emailEnabled = emailConfig.getBoolean("enabled")
  lazy val emailSmtpHost = emailConfig.getString("smtp-host")
  lazy val emailSmtpPort = emailConfig.getString("smtp-port")
  lazy val emailSmtpUserName = emailConfig.getString("smtp-username")
  lazy val emailSmtpPassword = emailConfig.getString("smtp-password")
  lazy val emailFrom = emailConfig.getString("from")
  lazy val emailEncoding = emailConfig.getString("encoding")
  lazy val emailSslConnection = emailConfig.getBoolean("ssl-connection")
  lazy val emailVerifySSLCertificate = emailConfig.getBoolean("verify-ssl-certificate")
}
