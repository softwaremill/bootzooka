/**
 * Copyright (C) 2009-2011 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import sbt._
import org.fusesource.scalate.sbt._

class Project(info: ProjectInfo) extends DefaultWebProject(info) with PrecompilerWebProject {

  lazy val fusesource_snapshot_repo = "FuseSource Snapshots" at
           "http://repo.fusesource.com/nexus/content/repositories/snapshots"
  lazy val java_net_repo = "Java.net Repository" at
           "http://download.java.net/maven/2"

  lazy val scalate_core     = "org.fusesource.scalate" % "scalate-core"      % "1.5.3" 
  lazy val servlet          = "javax.servlet"          % "servlet-api"       % "2.5" 
  lazy val jersey           = "com.sun.jersey"         % "jersey-server"     % "1.9" 
  lazy val logback          = "ch.qos.logback"         % "logback-classic"   % "0.9.26"

  // to get jetty-run working in sbt
  lazy val jetty_webapp     = "org.eclipse.jetty"      % "jetty-webapp"     % "7.0.2.RC0" % "test"

}
