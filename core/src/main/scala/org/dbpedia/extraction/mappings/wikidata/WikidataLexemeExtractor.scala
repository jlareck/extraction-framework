package org.dbpedia.extraction.mappings

import org.dbpedia.extraction.config.provenance.DBpediaDatasets
import org.dbpedia.extraction.ontology.Ontology
import org.dbpedia.extraction.transform.Quad
import org.dbpedia.extraction.util.{Language, WikidataUtil}
import org.dbpedia.extraction.wikiparser.{Namespace, JsonNode}
import org.wikidata.wdtk.datamodel.interfaces._

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer
import scala.language.reflectiveCalls

class WikidataLexemeExtractor(
                                 context: {
                                   def ontology: Ontology
                                   def language: Language
                                 }
                               )
  extends JsonNodeExtractor {

  private val lexicalCategoryProperty = "wikidata:lexicalCategory"
  private val descriptionProperty = context.ontology.properties("description")
//  private val languageProperty = context.ontology.properties("rdf:language")


  override val datasets = Set(DBpediaDatasets.WikidataProperty)


  override def extract(page: JsonNode, subjectUri: String): Seq[Quad] = {
    val quads = new ArrayBuffer[Quad]()

    val subject = WikidataUtil.getWikidataNamespace(subjectUri).replace("Lexeme:", "")

    quads ++= getLexicalCategory(page, subject)
//    quads ++= getDescriptions(page, subject)
//    quads ++= getLabels(page, subject)
//    quads ++= getStatements(page, subject)

    quads
  }


  private def getLexicalCategory(document: JsonNode, subjectUri: String): Seq[Quad] = {
    val quads = new ArrayBuffer[Quad]()

    val page = document.wikiDataDocument.deserializeLexemeDocument(document.wikiPage.source)

    if (document.wikiPage.title.namespace == Namespace.WikidataLexeme) {

      page.getLexicalCategory match {
        case value: Value =>{
          val objectValue = WikidataUtil.getValue(value)

        //  val datatype = if (WikidataUtil.getDatatype(v) != null) context.ontology.datatypes(WikidataUtil.getDatatype(v)) else null
          quads += new Quad(context.language, DBpediaDatasets.WikidataLexeme, subjectUri, lexicalCategoryProperty, objectValue,
            document.wikiPage.sourceIri, null)

        }
      }

    }
    quads
  }

//  private def getDescriptions(document: JsonNode, subjectUri: String): Seq[Quad] = {
//    val quads = new ArrayBuffer[Quad]()
//    val page = document.wikiDataDocument.deserializePropertyDocument(document.wikiPage.source)
//    if (document.wikiPage.title.namespace == Namespace.WikidataProperty) {
//      for ((lang, value) <- page.getDescriptions) {
//        val description = WikidataUtil.replacePunctuation(value.toString, lang)
//        Language.get(lang) match {
//          case Some(dbpedia_lang) => {
//            quads += new Quad(dbpedia_lang, DBpediaDatasets.WikidataProperty, subjectUri,
//              descriptionProperty, description, document.wikiPage.sourceIri, context.ontology.datatypes("rdf:langString"))
//          }
//          case _ =>
//        }
//      }
//    }
//    quads
//  }
//
//  private def getLabels(document: JsonNode, subjectUri: String): Seq[Quad] = {
//    val quads = new ArrayBuffer[Quad]()
//    val page = document.wikiDataDocument.deserializePropertyDocument(document.wikiPage.source)
//    if (document.wikiPage.title.namespace == Namespace.WikidataProperty) {
//      for ((lang, value) <- page.getLabels) {
//        val literalWithoutLang = WikidataUtil.replacePunctuation(value.toString, lang)
//        Language.get(lang) match {
//          case Some(dbpedia_lang) => {
//            quads += new Quad(dbpedia_lang, DBpediaDatasets.WikidataProperty,
//                subjectUri, labelProperty, literalWithoutLang, document.wikiPage.sourceIri, context.ontology.datatypes("rdf:langString"))
//          }
//          case _ =>
//        }
//      }
//    }
//    quads
//  }
//
//
//  private def getStatements(document: JsonNode, subjectUri: String): Seq[Quad] = {
//    val quads = new ArrayBuffer[Quad]()
//    val page = document.wikiDataDocument.deserializePropertyDocument(document.wikiPage.source)
//    if (document.wikiPage.title.namespace == Namespace.WikidataProperty) {
//      for (statementGroup <- page.getStatementGroups) {
//        statementGroup.foreach {
//          statement => {
//            val claim = statement.getClaim
//            val property = WikidataUtil.getWikidataNamespace(claim.getMainSnak().getPropertyId().getIri)
//
//            claim.getMainSnak() match {
//              case mainSnak: ValueSnak => {
//                val v = mainSnak.getValue
//                val value = WikidataUtil.getValue(v)
//                val datatype = if (WikidataUtil.getDatatype(v) != null) context.ontology.datatypes(WikidataUtil.getDatatype(v)) else null
//                quads += new Quad(context.language, DBpediaDatasets.WikidataProperty, subjectUri, property, value, document.wikiPage.sourceIri, datatype)
//              }
//              case _ =>
//            }
//          }
//        }
//      }
//    }
//
//    quads
//  }
}