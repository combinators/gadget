package example.timeGadget

import org.combinators.cls.types._
import org.combinators.cls.types.syntax._
import time.{FeatureUnit, FrequencyUnit, TemperatureUnit}


trait SemanticTypes {

  val temperatureUnit = Variable("TemperatureUnit")
  val frequencyUnit = Variable("FrequencyUnit")
  val featureType = Variable("FeatureType")

  val temperatureUnits: Kinding =
    TemperatureUnit.values().foldLeft(Kinding(temperatureUnit)) {
      case (k, unit) => k.addOption(feature.temperature(unit))
    }

  val frequencyUnits: Kinding =
    FrequencyUnit.values().foldLeft(Kinding(frequencyUnit)) {
      case (k, unit) => k.addOption(feature.extrema(unit))
    }

  val featureTypes: Kinding =
    FeatureUnit.values().foldLeft(Kinding(featureType)) {
      case (k, unit) => k.addOption(feature(unit))
    }.addOption(Omega)

  val kinding:Kinding =
    temperatureUnits
      .merge(frequencyUnits)
      .merge(featureTypes)

  /** Convert each frequency into corresponding seconds. */
  def frequencyToSecond(f:FrequencyUnit): Long = f match {
    case FrequencyUnit.Second => 1
    case FrequencyUnit.Minute => 60
    case FrequencyUnit.Hour => 60*60
    case FrequencyUnit.Day => 24*60*60
    case FrequencyUnit.Week => 7*24*60*60
    case FrequencyUnit.Month => 30*24*60*60
    case FrequencyUnit.Year => 365*24*60*60
  }

  // known capabilities of the gadget. Each new feature is encapsulated here
  object feature {
    def apply(ft: FeatureUnit): Type = 'Feature(Constructor(ft.toString))

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
      def apply(in: FrequencyUnit): Type = 'Extrema(Constructor(in.toString))

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



//
//  // when you want subtyping use a taxonomy
//  // by making this a FIELD it will be detected and then accessible. Note
//  // these params are strings but they will be referenced as 'Kelvin, i.e.
//  val taxonomyScales = Taxonomy("Scale").
//    addSubtype(scale.celsius.toString).
//    addSubtype(scale.fahrenheit.toString).
//    addSubtype(scale.kelvin.toString)
