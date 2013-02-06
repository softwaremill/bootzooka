package pl.softwaremill.bootstrap.test

import org.jruby.embed.{PathType, ScriptingContainer}
import org.jruby.RubyFixnum
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec

class SimpleRubyTest extends FlatSpec with ShouldMatchers {
  "This test" should "start jruby" in {
    val container = new ScriptingContainer()
    container.put("@filepath", "bootstrap-ui-tests/src/test/ruby/register_spec.rb")
    val script = container.parse(PathType.RELATIVE, "bootstrap-ui-tests/src/test/ruby/simplescript.rb")
    val executionResult:RubyFixnum = script.run().asInstanceOf[RubyFixnum]
    executionResult.to_s.asJavaString() should be ("0")
  }
}
