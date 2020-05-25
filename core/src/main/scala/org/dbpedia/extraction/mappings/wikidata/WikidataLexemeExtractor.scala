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

  private val lexicalCategoryProperty = "http://dbpedia.org/ontology/lexicalcategory"
  private val lemmaProperty = "http://dbpedia.org/ontology/lemma"
  private val language = "http://dbpedia.org/ontology/language"
  // private val descriptionProperty = context.ontology.properties("description")
  //  private val languageProperty = context.ontology.properties("rdf:language")


  override val datasets = Set(DBpediaDatasets.WikidataLexeme)


  override def extract(page: JsonNode, subjectUri: String): Seq[Quad] = {
    val quads = new ArrayBuffer[Quad]()

    val subject = WikidataUtil.getWikidataNamespace(subjectUri).replace("Lexeme:", "")

    //checks if extractor is used for correct entity
    if(page.wikiPage.title.namespace.code == 146){
      quads ++= getLexicalCategory(page, subject)
      quads ++= getLemmas(page, subject)
      quads ++= getLanguage(page, subject)
      quads ++= getStatements(page, subject)
    }



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
        case _ =>
      }
    }
    quads
  }

  private def getLemmas(document: JsonNode, subjectUri: String): Seq[Quad] = {
    val quads = new ArrayBuffer[Quad]()
    val page = document.wikiDataDocument.deserializeLexemeDocument(document.wikiPage.source)
    if (document.wikiPage.title.namespace == Namespace.WikidataLexeme) {
      for ((lang, value) <- page.getLemmas) {
        val lemmas = WikidataUtil.replacePunctuation(value.toString, lang)
        Language.get(lang) match {
          case Some(dbpedia_lang) => {
            quads += new Quad(dbpedia_lang, DBpediaDatasets.WikidataLexeme, subjectUri, lemmaProperty, lemmas,
              document.wikiPage.sourceIri, context.ontology.datatypes("rdf:langString"))
          }
          case _ =>
        }
      }
    }
    quads
  }

  private def getLanguage(document: JsonNode, subjectUri: String): Seq[Quad] = {
    val quads = new ArrayBuffer[Quad]()

    val page = document.wikiDataDocument.deserializeLexemeDocument(document.wikiPage.source)

    if (document.wikiPage.title.namespace == Namespace.WikidataLexeme) {

      page.getLanguage match {
        case value: Value =>{
          val objectValue = WikidataUtil.getValue(value)

          //  val datatype = if (WikidataUtil.getDatatype(v) != null) context.ontology.datatypes(WikidataUtil.getDatatype(v)) else null
          quads += new Quad(context.language, DBpediaDatasets.WikidataLexeme, subjectUri, language, objectValue,
            document.wikiPage.sourceIri, null)

        }
        case _ =>
      }
    }
    quads
  }
  private def getStatements(document: JsonNode, subjectUri: String): Seq[Quad] = {
    val quads = new ArrayBuffer[Quad]()
    val page = document.wikiDataDocument.deserializeLexemeDocument(document.wikiPage.source)
    if (document.wikiPage.title.namespace == Namespace.WikidataLexeme) {
      for (statementGroup <- page.getStatementGroups) {
        statementGroup.foreach {
          statement => {
            val claim = statement.getClaim
            val lexeme = WikidataUtil.getWikidataNamespace(claim.getMainSnak.getPropertyId.getIri)

            claim.getMainSnak match {
              case mainSnak: ValueSnak => {
                val v = mainSnak.getValue
                val value = WikidataUtil.getValue(v)
                val datatype = if (WikidataUtil.getDatatype(v) != null) context.ontology.datatypes(WikidataUtil.getDatatype(v)) else null
                quads += new Quad(context.language, DBpediaDatasets.WikidataLexeme, subjectUri, lexeme, value, document.wikiPage.sourceIri, datatype)
              }
              case _ =>
            }
          }
        }
      }
    }

    quads
  }
  //write senses
  private def getSenses(document: JsonNode, subjectUri: String): Seq[Quad] = {
    val quads = new ArrayBuffer[Quad]()
    val page = document.wikiDataDocument.deserializeLexemeDocument(document.wikiPage.source)
    if (document.wikiPage.title.namespace == Namespace.WikidataLexeme) {
      for (statementGroup <- page.getStatementGroups) {
        statementGroup.foreach {
          statement => {
            val claim = statement.getClaim
            val lexeme = WikidataUtil.getWikidataNamespace(claim.getMainSnak.getPropertyId.getIri)

            claim.getMainSnak match {
              case mainSnak: ValueSnak => {
                val v = mainSnak.getValue
                val value = WikidataUtil.getValue(v)
                val datatype = if (WikidataUtil.getDatatype(v) != null) context.ontology.datatypes(WikidataUtil.getDatatype(v)) else null
                quads += new Quad(context.language, DBpediaDatasets.WikidataLexeme, subjectUri, lexeme, value, document.wikiPage.sourceIri, datatype)
              }
              case _ =>
            }
          }
        }
      }
    }

    quads
  }

}