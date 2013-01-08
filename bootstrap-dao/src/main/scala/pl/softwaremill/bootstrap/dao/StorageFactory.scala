package pl.softwaremill.bootstrap.dao

import com.mongodb.casbah.TypeImports.MongoDB

trait StorageFactory {

  def userDAO: UserDAO

  def entryDAO: EntryDAO

  def codeDAO: PasswordResetCodeDAO

}

class MongoFactory(implicit val mongo: MongoDB) extends StorageFactory {

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

