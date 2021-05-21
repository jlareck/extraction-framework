package org.dbpedia.extraction.mappings

import java.util.logging.Logger

import org.dbpedia.extraction.config.Config
import org.dbpedia.extraction.config.provenance.DBpediaDatasets
import org.dbpedia.extraction.ontology.Ontology
import org.dbpedia.extraction.transform.{Quad, QuadBuilder}
import org.dbpedia.extraction.util.{Language, MediaWikiConnector}
import org.dbpedia.extraction.wikiparser.{Namespace, WikiPage}

class NewAbstractExtractor(
                    context : {
                      def ontology : Ontology
                      def language : Language
                      def configFile : Config
                    }
                  )
  extends WikiPageExtractor {
  protected val logger = Logger.getLogger(classOf[AbstractExtractor].getName)
  this.getClass.getClassLoader.getResource("myproperties.properties")


  /** protected params ... */

  protected val language = context.language.wikiCode

  //private val apiParametersFormat = "uselang="+language+"&format=xml&action=parse&prop=text&title=%s&text=%s"
  protected val apiParametersFormat ="format=json&action=query&prop=extracts&exlimit=max&explaintext&exintro&pageids=%s&redirects="

  // lazy so testing does not need ontology
  protected lazy val shortProperty = context.ontology.properties(context.configFile.abstractParameters.shortAbstractsProperty)

  // lazy so testing does not need ontology
  protected lazy val longProperty = context.ontology.properties(context.configFile.abstractParameters.longAbstractsProperty)

  protected lazy val longQuad = QuadBuilder(context.language, DBpediaDatasets.LongAbstracts, longProperty, null) _
  protected lazy val shortQuad = QuadBuilder(context.language, DBpediaDatasets.ShortAbstracts, shortProperty, null) _

  override val datasets = Set(DBpediaDatasets.LongAbstracts, DBpediaDatasets.ShortAbstracts)

  private val mwConnector = new MediaWikiConnector(context.configFile.mediawikiConnection, context.configFile.abstractParameters.abstractTags.split(","))


  override def extract(pageNode : WikiPage, subjectUri: String): Seq[Quad] =
  {
    //Only extract abstracts for pages from the Main namespace
    if(pageNode.title.namespace != Namespace.Main)
      return Seq.empty

    //Don't extract abstracts from redirect and disambiguation pages
    if(pageNode.isRedirect || pageNode.isDisambiguation)
      return Seq.empty

    //Retrieve page text
    val text = mwConnector.retrievePage(pageNode.title, apiParametersFormat, pageNode.isRetry, false) match{
      case Some(t) => t
      case None => return Seq.empty
    }

    val quadLong = longQuad(pageNode.uri, text, pageNode.sourceIri)

    Seq(quadLong)
  }
}