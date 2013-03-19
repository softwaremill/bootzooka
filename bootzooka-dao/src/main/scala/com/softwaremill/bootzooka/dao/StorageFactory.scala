package com.softwaremill.bootzooka.dao

trait StorageFactory {

  def userDAO: UserDAO

  def entryDAO: EntryDAO

  def codeDAO: PasswordResetCodeDAO

}

class MongoFactory extends StorageFactory {

  def userDAO = {
    new MongoUserDAO
  }

  def entryDAO = {
    new MongoEntryDAO
  }

  def codeDAO = {
    new MongoPasswordResetCodeDAO()
  }

}

class InMemoryFactory() extends StorageFactory {

  def userDAO = {
    new InMemoryUserDAO
  }

  def entryDAO = {
    new InMemoryEntryDAO()
  }

  def codeDAO = {
    new InMemoryPasswordResetCodeDAO()
  }

}

