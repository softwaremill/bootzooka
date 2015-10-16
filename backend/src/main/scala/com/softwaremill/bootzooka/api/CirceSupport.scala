package com.softwaremill.bootzooka.api

import io.circe.generic.{GenericInstances, LabelledInstances, BaseInstances}

trait CirceSupport extends BaseInstances with LabelledInstances with GenericInstances with CirceEncoders
