/*
 * COPYRIGHT (c) 2016 VOCADO, LLC.  ALL RIGHTS RESERVED.  THIS SOFTWARE CONTAINS
 * TRADE SECRETS AND/OR CONFIDENTIAL INFORMATION PROPRIETARY TO VOCADO, LLC AND/OR
 * ITS LICENSORS. ACCESS TO AND USE OF THIS INFORMATION IS STRICTLY LIMITED AND
 * CONTROLLED BY VOCADO, LLC.  THIS SOFTWARE MAY NOT BE COPIED, MODIFIED, DISTRIBUTED,
 * DISPLAYED, DISCLOSED OR USED IN ANY WAY NOT EXPRESSLY AUTHORIZED BY VOCADO, LLC IN WRITING.
 */

package com.softwaremill.bootzooka.common

import java.util.Random

object Utils {

  def randomString(length: Int) = {
    val sb = new StringBuffer()
    val r = new Random()

    for (i <- 1 to length) {
      sb.append((r.nextInt(25) + 65).toChar) // A - Z
    }

    sb.toString
  }

  // see http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
  private val hexArray = "0123456789ABCDEF".toCharArray
  def toHex(bytes: Array[Byte]): String = {
    val hexChars = new Array[Char](bytes.length * 2)
    for (j <- bytes.indices) {
      val v = bytes(j) & 0xFF
      hexChars(j * 2) = hexArray(v >>> 4)
      hexChars(j * 2 + 1) = hexArray(v & 0x0F)
    }
    new String(hexChars)
  }

  /**
   * Based on scala.xml.Utility.escape.
   * Escapes the characters &lt; &gt; &amp; and &quot; from string.
   */
  def escapeHtml(text: String): String = {
    object Escapes {
      /**
       * For reasons unclear escape and unescape are a long ways from
       * being logical inverses.
       */
      val pairs = Map(
        "lt" -> '<',
        "gt" -> '>',
        "amp" -> '&',
        "quot" -> '"'
      // enigmatic comment explaining why this isn't escaped --
      // is valid xhtml but not html, and IE doesn't know it, says jweb
      // "apos"  -> '\''
      )
      val escMap = pairs map { case (s, c) => c -> ("&%s;" format s) }
      val unescMap = pairs ++ Map("apos" -> '\'')
    }

    /**
     * Appends escaped string to `s`.
     */
    def escape(text: String, s: StringBuilder): StringBuilder = {
      // Implemented per XML spec:
      // http://www.w3.org/International/questions/qa-controls
      // imperative code 3x-4x faster than current implementation
      // dpp (David Pollak) 2010/02/03
      val len = text.length
      var pos = 0
      while (pos < len) {
        text.charAt(pos) match {
          case '<' => s.append("&lt;")
          case '>' => s.append("&gt;")
          case '&' => s.append("&amp;")
          case '"' => s.append("&quot;")
          case '\n' => s.append('\n')
          case '\r' => s.append('\r')
          case '\t' => s.append('\t')
          case c => if (c >= ' ') s.append(c)
        }

        pos += 1
      }
      s
    }

    val sb = new StringBuilder
    escape(text, sb)
    sb.toString()
  }

}
