package org.combinators.gadget.combinators

trait CodeGeneratorTask[Result] {
  def run(): Result
}

object CodeGeneratorTask {
  type Modification[T <: CodeGeneratorTask[_]] = T => T

  def apply[R](toTask: => R) = new CodeGeneratorTask[R] {
    def run(): R = toTask
  }
}