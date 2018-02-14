package example.timeGadget

import javax.inject.Inject

import com.github.javaparser.ast.CompilationUnit
import org.combinators.cls.interpreter.ReflectedRepository
import org.combinators.cls.git._
import org.combinators.cls.types.{Constructor, Type}
import org.combinators.templating.persistable.JavaPersistable._
import org.webjars.play.WebJarsUtil
import play.api.inject.ApplicationLifecycle
import time._
import org.combinators.cls.types.syntax._

import scala.collection.JavaConverters._

class TimeGadget @Inject()(webJars: WebJarsUtil, applicationLifecycle: ApplicationLifecycle) extends InhabitationController(webJars, applicationLifecycle) with RoutingEntries   {

  // domain model. Add the features you want, in the order they will be processed
  //    Weather gets checked every thirty minutes
  //    Extremes use sliding window with Daily time period
  val min30 = new FrequencyFeature(30, FrequencyUnit.Minute)
  val weatherFeature = new WeatherFeature("Worcester", "01609", TemperatureUnit.Fahrenheit, min30)
  val extremeFeature = new ExtremaFeature(FrequencyUnit.Day)
  val domainModel:Gadget = new Gadget()
    .add(weatherFeature)
    .add(extremeFeature)

  lazy val repository = new Concepts(domainModel) {}
  import repository._

  lazy val Gamma = ReflectedRepository(repository, substitutionSpace=kinding, classLoader = this.getClass.getClassLoader)
        .addCombinator(new Combined_Temp_Extrema())    // should be inferred from the domain...
        .addCombinator(new Combined_Temp_Extrema_Code(weatherFeature.temperatureUnit))
        .addCombinator(new CurrentTemperature(weatherFeature))
        .addCombinator(new MainCode())

  lazy val combinatorComponents = Gamma.combinatorComponents

  // request artifacts for the solution domain. Call for a main program with features
  // seek inhabitation for all features associated with the model, in addition to core gadget code
  //lazy val targets = domainModel.iterator.asScala.map{ f => artifact(artifact.extraCode, feature(f)) }.toSeq

  lazy val targets:Seq[Type] = Seq.empty :+ artifact(artifact.mainProgram, feature(FeatureUnit.Weather) :&: feature(FeatureUnit.Extrema) :&: feature(FeatureUnit.Frequency))

  //lazy val targets:Seq[Type] = Seq.empty :+ Constructor("All")

  //  artifact(artifact.mainProgram, featureType :&: feature(freqFeature))

  lazy val results:Results = EmptyInhabitationBatchJobResults(Gamma)
        .addJobs[CompilationUnit](targets)
        .compute()

  // EmptyInhabitationBatchJobResults(Gamma).addJobs[CompilationUnit](Synthesizer.allTargets(variation)).compute()

  lazy val controllerAddress: String = "gadget"
}
