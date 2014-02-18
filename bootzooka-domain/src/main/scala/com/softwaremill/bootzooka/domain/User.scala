package com.softwaremill.bootzooka.domain

import org.bson.types.ObjectId
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import com.softwaremill.bootzooka.common.Utils

case class User(id: ObjectId, login: String, loginLowerCased: String, email: String, password: String, salt: String,
                token: String)

object User {

  def apply(login: String, email: String, plainPassword: String, salt: String, token: String) = {
    new User(new ObjectId, login, login.toLowerCase, email, encryptPassword(plainPassword, salt), salt, token)
  }

  def encryptPassword(password: String, salt: String): String = {
    // 10k iterations takes about 10ms to encrypt a password on a 2013 MacBook
    val keySpec = new PBEKeySpec(password.toCharArray, salt.getBytes, 10000, 128)
    val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
    val bytes = secretKeyFactory.generateSecret(keySpec).getEncoded
    Utils.toHex(bytes)
  }

  def passwordsMatch(plainPassword: String, user: User) = {
    user.password.equals(encryptPassword(plainPassword, user.salt))
  }
}
