package pl.softwaremill.bootstrap.common

object Utils {

    def md5(s: String): String = {
        val m = java.security.MessageDigest.getInstance("MD5")
        val b = s.getBytes("UTF-8")
        m.update(b, 0, b.length)
        new java.math.BigInteger(1, m.digest()).toString(16)
    }

    def checkbox(s: String): Boolean = {
        s match {
            case "yes" => true
            case "y" => true
            case "1" => true
            case "true" => true
            case _ => false
        }
    }

}
