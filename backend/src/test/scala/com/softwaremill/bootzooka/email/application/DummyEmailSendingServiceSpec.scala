/*
 * COPYRIGHT (c) 2016 VOCADO, LLC.  ALL RIGHTS RESERVED.  THIS SOFTWARE CONTAINS
 * TRADE SECRETS AND/OR CONFIDENTIAL INFORMATION PROPRIETARY TO VOCADO, LLC AND/OR
 * ITS LICENSORS. ACCESS TO AND USE OF THIS INFORMATION IS STRICTLY LIMITED AND
 * CONTROLLED BY VOCADO, LLC.  THIS SOFTWARE MAY NOT BE COPIED, MODIFIED, DISTRIBUTED,
 * DISPLAYED, DISCLOSED OR USED IN ANY WAY NOT EXPRESSLY AUTHORIZED BY VOCADO, LLC IN WRITING.
 */

package com.softwaremill.bootzooka.email.application

import com.softwaremill.bootzooka.email.domain.EmailContentWithSubject
import org.scalatest.{FlatSpec, Matchers}

class DummyEmailSendingServiceSpec extends FlatSpec with Matchers {
  it should "send scheduled email" in {
    val service = new DummyEmailService
    service.scheduleEmail("test@sml.com", new EmailContentWithSubject("content", "subject"))
    service.wasEmailSent("test@sml.com", "subject") should be(true)
  }
}
