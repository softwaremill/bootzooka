package com.softwaremill.bootzooka.dao

import net.liftweb.record.Record
import net.liftweb.record.field.StringField

class LongStringField[OwnerType <: Record[OwnerType]](rec: OwnerType, maxLen: Int = Int.MaxValue) extends StringField(rec, maxLen)
