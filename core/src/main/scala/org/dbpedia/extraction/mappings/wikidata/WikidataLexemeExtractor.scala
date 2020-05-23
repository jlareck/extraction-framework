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

  private val lexicalCategoryProperty = "http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate"
  // private val descriptionProperty = context.ontology.properties("description")
  //  private val languageProperty = context.ontology.properties("rdf:language")


  override val datasets = Set(DBpediaDatasets.WikidataLexeme)


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


}