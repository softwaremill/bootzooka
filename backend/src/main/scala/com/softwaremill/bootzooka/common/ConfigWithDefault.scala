/*
 * COPYRIGHT (c) 2016 VOCADO, LLC.  ALL RIGHTS RESERVED.  THIS SOFTWARE CONTAINS
 * TRADE SECRETS AND/OR CONFIDENTIAL INFORMATION PROPRIETARY TO VOCADO, LLC AND/OR
 * ITS LICENSORS. ACCESS TO AND USE OF THIS INFORMATION IS STRICTLY LIMITED AND
 * CONTROLLED BY VOCADO, LLC.  THIS SOFTWARE MAY NOT BE COPIED, MODIFIED, DISTRIBUTED,
 * DISPLAYED, DISCLOSED OR USED IN ANY WAY NOT EXPRESSLY AUTHORIZED BY VOCADO, LLC IN WRITING.
 */

package com.softwaremill.bootzooka.common

import java.util.concurrent.TimeUnit

import com.typesafe.config.Config

trait ConfigWithDefault {

  def rootConfig: Config

  def getBoolean(path: String, default: Boolean) = ifHasPath(path, default) { _.getBoolean(path) }
  def getString(path: String, default: String) = ifHasPath(path, default) { _.getString(path) }
  def getInt(path: String, default: Int) = ifHasPath(path, default) { _.getInt(path) }
  def getConfig(path: String, default: Config) = ifHasPath(path, default) { _.getConfig(path) }
  def getMilliseconds(path: String, default: Long) = ifHasPath(path, default) { _.getDuration(path, TimeUnit.MILLISECONDS) }
  def getOptionalString(path: String, default: Option[String] = None) = getOptional(path) { _.getString(path) }

  private def ifHasPath[T](path: String, default: T)(get: Config => T): T = {
    if (rootConfig.hasPath(path)) get(rootConfig) else default
  }

  private def getOptional[T](fullPath: String, default: Option[T] = None)(get: Config => T) = {
    if (rootConfig.hasPath(fullPath)) {
      Some(get(rootConfig))
    }
    else {
      default
    }
  }

}
