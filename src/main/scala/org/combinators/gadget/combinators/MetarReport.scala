package org.combinators.gadget.combinators

import scala.collection.JavaConverters._
import com.github.javaparser.ast.ImportDeclaration
import com.github.javaparser.ast.body.{BodyDeclaration, EnumConstantDeclaration, EnumDeclaration, MethodDeclaration}
import com.github.javaparser.ast.expr.{Expression, Name}
import com.github.javaparser.ast.stmt.Statement
import org.combinators.cls.interpreter.combinator
import org.combinators.cls.types.Type
import org.combinators.gadget.{Feature, FeatureClassification, SemanticTypes}
import org.combinators.templating.twirl.Java
import org.combinators.cls.types.syntax._




trait MetarReport[R] { self: Gadget with SemanticTypes =>

  class MetarCodeFeature extends Feature {
    override def getClassification: FeatureClassification = FeatureClassification.Weather
  }

  val locationEnum: Name = Java("Locations").name()
  val airportInfoMap: Name = Java("airportInformation").name()

  case class MetarCode(
    locations: Set[EnumConstantDeclaration],
    methods: Map[Name, CodeGeneratorTask[MethodDeclaration]]
  ) extends CodeGeneratorTask[Seq[BodyDeclaration[_]]] {
    override def run(): Seq[BodyDeclaration[_]] =
      Java(
        s"""
           |enum $locationEnum {
           |  ${locations.mkString(", ")};
           |}
           |
           |public void updateAirportInformation($locationEnum forAirport) {
           |    URL url = new URL(String.format("https://aviationweather.gov/adds/dataserver_current/httpparam?dataSource=metars&requestType=retrieve&format=xml&hoursBeforeNow=1&mostRecent=true&stationString=%s", forAirport.toString()));
           |    try {
           |        $airportInfoMap.put(forAirport, DocumentBuilderFactory.newInstance().parse(url.openStream()));
           |    } catch (Exception ex) {
           |        ex.printStackTrace();
           |    }
           |}
           |
           |${methods.values.map(_.run()).mkString("\n")}
         """.stripMargin).classBodyDeclarations()
    def addLocation(location: String): MetarCode =
      copy(locations = locations + new EnumConstantDeclaration(location.capitalize))
    def addMethod(method: Name, methodTask: CodeGeneratorTask[MethodDeclaration]): MetarCode =
      copy(methods = methods.updated(method, methodTask))
  }


  @combinator object metarCode {
    def apply(
      importRegistry:
        CodeGeneratorTask.Modification[ImportDeclarationGenerator] => CodeGeneratorTask.Modification[R]
    ) = {

    }
  }

  /* side effect version

  @combinator object Locations {
    val locationsEnum: EnumDeclaration =
      Java(s"""enum Locations {}""").classBodyDeclaration().toEnumDeclaration.get
    def apply(featureCodeRegistry: (String, Seq[BodyDeclaration[_]]) => Unit): EnumDeclaration = {
      featureCodeRegistry("locationsEnum", locationsEnum)
      locationsEnum
    }
    val semanticType: Type = registry(featureCode) =>: metarReport.locations
  }

  @combinator object locationRegistry {
    def apply(locations: EnumDeclaration): String => Unit =
      airportLocation => {
        if (!locations.getEntries.asScala.exists(_.getNameAsString == airportLocation)) {
          locations.addEnumConstant(airportLocation)
        }
      }
    val semanticType: Type = metarReport.locations =>: registry(metarReport.locations)
  }

  @combinator object airportInformationMap {
    def apply(locations: EnumDeclaration,
      importRegistry: ImportDeclaration => Unit,
      featureCodeRegistry: (String, Seq[BodyDeclaration[_]]) => Unit): Name = {

      importRegistry(Java(s"import java.util.EnumMap;").importDeclaration())
      val name = Java("airportInformation").name()
      featureCodeRegistry(
        "airportInfo",
        Java(s"private final EnumMap<${locations.getName}, Document> $name;").classBodyDeclarations())
      name
    }
    val semanticType: Type =
      metarReport.locations =>:
        registry(imports) =>:
        registry(featureCode) =>:
        metarReport.airportInfoMap
  }

  @combinator object metarUpdateCode {
    def apply(
      locations: EnumDeclaration,
      airportInfoMap: Name,
      importRegistry: ImportDeclaration => Unit,
      featureCodeRegistry: (String, Seq[BodyDeclaration[_]]) => Unit): Name = {
      val imports =
        Seq(
          "java.net.Url",
          "javax.xml.parsers.DocumentBuilderFactory"
        )
      imports.foreach(imp => importRegistry(Java(s"import $imp;").importDeclaration()))
      featureCodeRegistry(
        "updateAirportInfo",
        Java(s"""
               |public void updateAirportInformation(${locations.getName} forAirport) {
               |    URL url = new URL(String.format("https://aviationweather.gov/adds/dataserver_current/httpparam?dataSource=metars&requestType=retrieve&format=xml&hoursBeforeNow=1&mostRecent=true&stationString=%s", forAirport.toString()));
               |    try {
               |        $airportInfoMap.put(${locations.getName}, DocumentBuilderFactory.newInstance().parse(url.openStream()));
               |    } catch (Exception ex) {
               |        ex.printStackTrace();
               |    }
               |}
              """.stripMargin).classBodyDeclarations())
      Java("updateAirportInformation").name()
    }
    val semanticType: Type =
      metarReport.locations =>:
        metarReport.airportInfoMap =>:
        registry(imports) =>:
        registry(featureCode) =>:
        metarReport.updateMethod
  }

  @combinator object metarXpathExtractor {
    def apply(
      locations: EnumDeclaration,
      airportInfoMap: Name,
      importRegistry: ImportDeclaration => Unit,
      featureCodeRegistry: (String, Seq[BodyDeclaration[_]]) => Unit): Name = {
      val imports =
        Seq(
          "org.w3c.dom.Document",
          "java.util.Optional",
          "javax.xml.xpath.XPathFactory"
        )
      imports.foreach(imp => importRegistry(Java(s"import $imp;").importDeclaration()))
      featureCodeRegistry(
        "metarValue",
        Java(s"""
                |public Optional<String> metarValue(String xpath, ${locations.getName} forAirport) {
                |    Document doc = airportInformation.get(forAirport);
                |    if (doc == null) {
                |        return Optional.empty();
                |    }
                |    try {
                |        NodeList nodes = XPathFactory.newInstance().newXPath().compile(xpath).evaluate(doc, XPathConstants.NODESET);
                |    } catch (Exception ex) {
                |        ex.printStackTrace();
                |        return Optional.empty();
                |    }
                |    if (nodes.getLength() != 1) {
                |        return Optional.empty();
                |    }
                |    return Optional.of(nodes.get(0).getNodeValue());
                |}
              """.stripMargin).classBodyDeclarations())
      Java("metarValue").name()
    }
    val semanticType: Type =
      metarReport.locations =>:
        metarReport.airportInfoMap =>:
        registry(imports) =>:
        registry(featureCode) =>:
        metarReport.metarValueMethod
  }

  class MetarMethod(
    extraImports: Seq[ImportDeclaration],
    resultType: com.github.javaparser.ast.expr.TypeExpr,
    methodName: Name,
    xpath: String,
    toResultCode: Expression => Seq[Statement],
    semanticMethodType: Type) {
    def apply(
      locations: EnumDeclaration,
      metarMethod: Name,
      importRegistry: ImportDeclaration => Unit,
      featureCodeRegistry: (String, Seq[BodyDeclaration[_]]) => Unit): Name = {
      val imports = "java.util.Optional"
      imports.foreach(imp => importRegistry(Java(s"import $imp;").importDeclaration()))
      extraImports.foreach(importRegistry(_))
      featureCodeRegistry(
        methodName,
        Java(s"""
                |public Optional<$resultType> $methodName(${locations.getName} forAirport) {
                |    Optional<String> observationDate = $metarMethod(${Java(s"$xpath").expression()}, forAirport);
                |    return Optional.map(v -> { ${toResultCode(Java("v").expression())} });
                |}
              """.stripMargin).classBodyDeclarations())
      methodName
    }
    val semanticType: Type = metarReport.metarMethodCombinatorType(semanticMethodType)

  }

  @combinator object metarObservationTime
    extends MetarMethod(
      Seq("java.time.LocalDateTime", "java.time.format.DateTimeFormatter")
        .map(imp => Java(s"import $imp;").importDeclaration()),
      Java("LocalDateTime").expression(),
      Java("observationTime").name(),
      "//observation_time",
      v => Java(s"return LocalDateTime.parse($v, DateTimeFormatter.ISO_OFFSET_DATE_TIME);").statements(),
      metarReport.observationTimeMethod
    ) */
}
