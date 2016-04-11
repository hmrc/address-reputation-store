package uk.co.hmrc.logging

/**
  * LoggingFacade interacts by side-effect and is intrinsically hard to auto-test,
  * but it is sufficient to demonstrate that it is working manually.
  */
object LoggingFacadeTest extends App {

  Stdout.info("T1 {} {}", "a", "b")
  Stdout.info("T2", new Exception("foo1"))

  Stdout.warn("T3 {} {}", "a", "b")
  Stdout.warn("T4", new Exception("foo2"))

  Stdout.error("T5 {} {}", "a", "b")
  Stdout.error("T6", new Exception("foo3"))
}
