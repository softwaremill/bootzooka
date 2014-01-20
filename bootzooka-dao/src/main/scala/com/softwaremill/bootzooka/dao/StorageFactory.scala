package com.softwaremill.bootzooka.dao

trait StorageFactory {

  def userDAO: UserDAO

  def codeDAO: PasswordResetCodeDAO

}

class MongoFactory extends StorageFactory {

  def userDAO = {
    new MongoUserDAO
  }

  def codeDAO = {
    new MongoPasswordResetCodeDAO()
  }

}

class InMemoryFactory() extends StorageFactory {

  def userDAO = {
    new InMemoryUserDAO
  }

  def codeDAO = {
    new InMemoryPasswordResetCodeDAO()
  }

}

