package org.combinators.gadget.combinators

import java.math.BigInteger

import com.github.javaparser.ast.{CompilationUnit, ImportDeclaration}
import com.github.javaparser.ast.body.{BodyDeclaration, MethodDeclaration}
import com.github.javaparser.ast.stmt.Statement
import org.combinators.cls.interpreter.combinator
import org.combinators.cls.types.Type
import org.combinators.cls.types.syntax._
import org.combinators.gadget.{Feature, PeriodicFeature, SemanticTypes}
import org.combinators.templating.twirl.Java

trait Gadget { self: SemanticTypes =>
  case class GadgetGenerator(
    importDeclarations: CodeGeneratorTask[Set[ImportDeclaration]] = ImportDeclarationGenerator(),
    featureCode: CodeGeneratorTask[Seq[BodyDeclaration[_]]] = FeatureCodeGeneratorRegistry(),
    mainMethodCode: CodeGeneratorTask[MethodDeclaration] = MainMethodCodeGeneratorTask()
  ) extends CodeGeneratorTask[CompilationUnit] {
    def run(): CompilationUnit =
      Java(
        s"""
           |${importDeclarations.run().mkString("\n")}
           |class Gadget {
           |  ${featureCode.run().mkString("\n")}
           |  ${mainMethodCode.run()}
           |}
         """.stripMargin).compilationUnit()
  }

  case class ImportDeclarationGenerator(imports: Set[ImportDeclaration] = Set.empty) extends CodeGeneratorTask[Set[ImportDeclaration]] {
    def run(): Set[ImportDeclaration] = imports

    def addImport(importDeclaration: ImportDeclaration): ImportDeclarationGenerator =
      copy(imports = imports + importDeclaration)
  }

  case class FeatureCodeGeneratorRegistry(
    features: Map[Feature, CodeGeneratorTask[Seq[BodyDeclaration[_]]]] = Map.empty
  ) extends CodeGeneratorTask[Seq[BodyDeclaration[_]]] {
    def run(): Seq[BodyDeclaration[_]] = features.values.flatMap(_.run()).toSeq

    def updateFeature(feature: Feature)
      (updateWith: CodeGeneratorTask.Modification[CodeGeneratorTask[Seq[BodyDeclaration[_]]]]): FeatureCodeGeneratorRegistry = {
      val oldFeature = features.getOrElse(feature, CodeGeneratorTask { Seq.empty })
      copy(features = features.updated(feature, updateWith(oldFeature)))
    }
  }

  case class MainMethodCodeGeneratorTask(
    featureExecutionCode: Seq[CodeGeneratorTask[Seq[Statement]]] = Seq.empty
  ) extends CodeGeneratorTask[MethodDeclaration] {
    def run(): MethodDeclaration =
      Java(
        s"""
           |public static void main() {
           |  ${featureExecutionCode.flatMap(_.run()).mkString("\n")}
           |}
         """.stripMargin).methodDeclarations().head
  }

  case class SchedulerCodeGeneratorTask(
    featureExecutionRegistry: Map[PeriodicFeature, CodeGeneratorTask[Seq[Statement]]] = Map.empty
  ) extends CodeGeneratorTask[Seq[Statement]] {
    def run(): Seq[Statement] = if (featureExecutionRegistry.isEmpty) Seq.empty else {
      val intervals: Seq[BigInteger] =
        featureExecutionRegistry.keys.map(f => BigInteger.valueOf(f.interval)).toSeq
      val schedulingInterval: Int =
        intervals.reduce((x, y) => x.gcd(y)).intValue
      val schedulingWrapAround: Int =
        intervals.reduce((x, y) => x.multiply(y)).divide(BigInteger.valueOf(schedulingInterval)).intValue()
      val groupedFeatures: Map[Int, Seq[Statement]] =
        featureExecutionRegistry.groupBy(_._1.interval).mapValues(_.values.flatMap(_.run()).toSeq)
      val featureExecutionCode: Seq[Statement] =
        groupedFeatures.foldLeft(Seq.empty[Statement]) { case (code, (interval, executionCode)) =>
          code ++ Java(
            s"""
               |if (tick % $interval == 0) {
               |    ${executionCode.mkString("\n")}
               |}
             """.stripMargin).statements()
        }
      Java(
        s"""
           |int tick = 0;
           |while (true) {
           |  $featureExecutionCode
           |  try { Thread.sleep($schedulingInterval); } catch (Exception ex) {}
           |  tick = (tick + $schedulingInterval) % $schedulingWrapAround;
           |}
         """.stripMargin).statements()
    }

    def updateFeature(feature: PeriodicFeature)
      (updateWith: CodeGeneratorTask.Modification[CodeGeneratorTask[Seq[Statement]]]): SchedulerCodeGeneratorTask = {
      val oldFeature = featureExecutionRegistry.getOrElse(feature, CodeGeneratorTask { Seq.empty })
      copy(featureExecutionRegistry = featureExecutionRegistry.updated(feature, updateWith(oldFeature)))
    }
  }


  @combinator object importModifier {
    def apply: CodeGeneratorTask.Modification[ImportDeclarationGenerator] => CodeGeneratorTask.Modification[GadgetGenerator] =
      mod => gen => gen.copy(importDeclarations = gen.importDeclarations match {
        case importDeclGen: ImportDeclarationGenerator => mod(importDeclGen)
        case _ => mod(ImportDeclarationGenerator())
        })

    val semanticType: Type = registry(imports)
  }

  @combinator object featureModifier {
    def apply: CodeGeneratorTask.Modification[FeatureCodeGeneratorRegistry] => CodeGeneratorTask.Modification[GadgetGenerator] =
      mod => gen =>
        gen.copy(featureCode = gen.featureCode match {
          case featureCodeGen: FeatureCodeGeneratorRegistry => mod(featureCodeGen)
          case _ => mod(FeatureCodeGeneratorRegistry())
        })

    val semanticType: Type = registry(featureCode)
  }

  @combinator object mainCodeModifier {
    def apply: CodeGeneratorTask.Modification[MainMethodCodeGeneratorTask] => CodeGeneratorTask.Modification[GadgetGenerator] =
      mod => gen =>
        gen.copy(mainMethodCode =
          gen.mainMethodCode match {
            case mainMethodCode: MainMethodCodeGeneratorTask => mod(mainMethodCode)
            case _ => mod(MainMethodCodeGeneratorTask())
          })
    val semanticType: Type = registry(mainCode)
  }

  @combinator object schedulerModifier {
    def apply(
      mainMethodModifier:
        CodeGeneratorTask.Modification[MainMethodCodeGeneratorTask] =>
        CodeGeneratorTask.Modification[GadgetGenerator]):
    CodeGeneratorTask.Modification[SchedulerCodeGeneratorTask] => CodeGeneratorTask.Modification[GadgetGenerator] =
      mod => mainMethodModifier(main => {
        val hasGen = main.featureExecutionCode.exists {
          case _: SchedulerCodeGeneratorTask => true
          case _ => false
        }
        val newFeatures =
          if (!hasGen) {
            main.featureExecutionCode :+ mod(SchedulerCodeGeneratorTask())
          } else {
            main.featureExecutionCode.map {
              case sched: SchedulerCodeGeneratorTask => mod(sched)
              case x => x
            }
          }

        main.copy(featureExecutionCode = newFeatures)
      })
    val semanticType: Type = registry(mainCode) =>: registry(featureExecutionCode)
  }
}
