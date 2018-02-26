package org.combinators.gadget

import java.util.stream.Collectors

import org.combinators.cls.types._
import org.combinators.cls.types.syntax._
import org.combinators.gadget.{FeatureClassification, TemperatureUnit, TimeUnit}
import shapeless.feat.Finite

import scala.collection.JavaConverters._

trait SemanticTypes {

  object registry {
    def apply(ofConcept: Type): Type = 'Registry(ofConcept)
  }

  object metarReport {
    val locations: Type = 'Locations
    val airportInfoMap: Type = 'AirportInfoMap
    val updateMethod: Type = 'AirportUpdateMethod
    val observationTimeMethod: Type = 'ObservationTimeMethod
    val metarValueMethod: Type = 'MetarValueMethod

    object metarMethodCombinatorType {
      def apply(semanticMethodType: Type) =
        metarReport.locations =>:
        metarReport.metarValueMethod =>:
        registry(imports) =>:
        registry(featureCode) =>:
        semanticMethodType
    }
  }

  object temperature {
    object unit {
      def apply(unit: TemperatureUnit): Type = Constructor(unit.toString)
    }
    object temperatureMethod {
      def apply(unit: Type): Type = 'TemperatureMethod(unit)
    }

    object conversion {
      def apply(fromUnit: Type, toUnit: Type): Type = 'Conversion(fromUnit, fromUnit)
    }
  }

  val imports: Type = 'Imports
  val featureCode: Type = 'FeatureCode
  val featureExecutionCode: Type = 'FeatureExecutionCode
  val mainCode: Type = 'MainCode
}

/*class SemanticTypes(gadget: Gadget) {

  val temperatureUnit = Variable("TemperatureUnit")
  val frequencyUnit = Variable("TimeUnit")
  val featureType = Variable("FeatureType")

  val temperatureUnits: Kinding =
    TemperatureUnit.values().foldLeft(Kinding(temperatureUnit)) {
      case (k, unit) => k.addOption(feature.temperature(unit))
    }

  val frequencyUnits: Kinding =
    TimeUnit.values().foldLeft(Kinding(frequencyUnit)) {
      case (k, unit) => k.addOption(feature.extrema(unit))
    }

  val featureTypes: Kinding =
    FeatureClassification.values().foldLeft(Kinding(featureType)) {
      case (k, unit) => k.addOption(feature(unit))
    }.addOption(Omega)

  val kinding:Kinding =
    temperatureUnits
      .merge(frequencyUnits)
      .merge(featureTypes)

  /** Convert each frequency into corresponding seconds. */
  def frequencyToSecond(f:TimeUnit): Long = f match {
    case TimeUnit.Second => 1
    case TimeUnit.Minute => 60
    case TimeUnit.Hour => 60*60
    case TimeUnit.Day => 24*60*60
    case TimeUnit.Week => 7*24*60*60
    case TimeUnit.Month => 30*24*60*60
    case TimeUnit.Year => 365*24*60*60
  }

  // known capabilities of the gadget. Each new feature is encapsulated here
  object feature {
    def apply(ft: FeatureClassification): Type = 'Feature(Constructor(ft.toString))

    // Temperature Feature identified.
    object temperature {
      def apply(in: TemperatureUnit): Type = 'TemperatureIn(Constructor(in.toString))

      object converter {
        def apply(from: TemperatureUnit, forUnit: Type):Type =
          'Converter(Constructor(from.toString()), forUnit)
      }
    }

    // Record extreme ranges of temperature
    object extrema {
      def apply(in: TimeUnit): Type = 'Extrema(Constructor(in.toString))

      object converter {
        def apply(from: Type, to: Type):Type = 'Converter(from, to)
      }
    }

    // default feature always present
    val time: Type = 'Time
  }

  /**
    * There are a number of coding artifacts needed:
    *
    * 1. extraCode
    */
  object artifact {
    def apply(part:Type, forFeature: Type):Type = 'Artifact(part, forFeature)

    val extraCode: Type      = 'ImplementationOf
    val combinedCode: Type   = 'Combined
    val loopCode: Type       = 'LoopCodeFor
    val mainProgram: Type    = 'ProgramWith
  }



}
*/


//
//  // when you want subtyping use a taxonomy
//  // by making this a FIELD it will be detected and then accessible. Note
//  // these params are strings but they will be referenced as 'Kelvin, i.e.
//  val taxonomyScales = Taxonomy("Scale").
//    addSubtype(scale.celsius.toString).
//    addSubtype(scale.fahrenheit.toString).
//    addSubtype(scale.kelvin.toString)
