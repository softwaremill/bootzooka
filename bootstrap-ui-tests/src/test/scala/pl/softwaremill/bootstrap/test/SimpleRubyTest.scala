package pl.softwaremill.bootstrap.test

import org.specs2.mutable.Specification
import org.jruby.embed.{PathType, ScriptingContainer}

/**
 * .
 */
class SimpleRubyTest extends Specification {
  "This test" should {
    "start jruby" in {
      val container = new ScriptingContainer()
      container.put("@filepath", "bootstrap-ui-tests/src/test/ruby/register_spec.rb")
      container.put("errs", container.getError)
      container.put("output", container.getOutput)
      val script = container.parse(PathType.RELATIVE, "bootstrap-ui-tests/src/test/ruby/simplescript.rb")
      script.run() must not be null
    }
  }
}
