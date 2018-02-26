package org.combinators.gadget.combinators

import com.github.javaparser.ast.ImportDeclaration
import com.github.javaparser.ast.body.{BodyDeclaration, EnumDeclaration}
import com.github.javaparser.ast.expr.{Expression, Name}
import org.combinators.cls.interpreter.combinator
import org.combinators.cls.types.Type
import org.combinators.cls.types.syntax._
import org.combinators.gadget.{SemanticTypes, TemperatureFeature, TemperatureUnit}
import org.combinators.templating.twirl.Java

trait Temperature { self: SemanticTypes with MetarReport =>
  val feature: TemperatureFeature

  @combinator object temperatureMethod {
    def apply(converterCode: Expression => Expression,
      locations: EnumDeclaration,
      metarMethod: Name,
      importRegistry: ImportDeclaration => Unit,
      featureCodeRegistry: (String, Seq[BodyDeclaration[_]]) => Unit): Name = {
      val method = new MetarMethod(
        Seq.empty,
        Java("double").expression(),
        Java("temperatureFor").name(),
        "//temp_c",
        v => Java(s"return ${converterCode(Java(s"Double.valueOf($v)").expression())};").statements(),
        temperature.temperatureMethod(temperature.unit(feature.temperatureUnit))
      )
      method.apply(locations, metarMethod, importRegistry, featureCodeRegistry)
    }
    val semanticType: Type =
      temperature.conversion(temperature.unit(TemperatureUnit.Celsius), temperature.unit(feature.temperatureUnit)) =>:
      metarReport.metarMethodCombinatorType(temperature.temperatureMethod(temperature.unit(feature.temperatureUnit)))
  }

  class temperatureCoversion(from: TemperatureUnit, to: TemperatureUnit, code: Expression => Expression) {
    def apply(): Expression => Expression = code
    val semanticType: Type = temperature.conversion(temperature.unit(from), temperature.unit(to))
  }
  @combinator object celsiusToCelsius
    extends temperatureCoversion(TemperatureUnit.Celsius, TemperatureUnit.Celsius, x => x)
  @combinator object celsiusToFahrenheit
    extends temperatureCoversion(
      TemperatureUnit.Celsius,
      TemperatureUnit.Fahrenheit,
      x => Java(s"($x * 1.8 + 32.0)").expression())

  /* TODO:
   Scheduler registry: (TaskName, Interval, Seq[Statement])) => Unit
   TemperatureTask
    ((Name, Name, Name, SchedulerRegistry) => (String, Exp => Unit) => Unit) :&:
    (metarReport.observationTimeMethod =>:
     metarReport.updateMethod =>:
     metarReport.metarMethodCombinatorType(temperature.temperatureMethod(temperature.unit(feature.temperatureUnit))) =>:
     schedulerRegistry =>:
     temperature.taskRegistry)
  check observation time; if necessary call update; get temperature; run registered callbacks; sysout temperature

   ExtremaRecordTask
    (((String, Exp => Unit) => Unit) => Unit) :&:
    (temperature.taskRegistry =>:
     extrema.recordTask)
   record observation
   adapt to deal with arbitrary obeservation exp instead of temperature
   */
}
