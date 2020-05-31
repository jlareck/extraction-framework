package org.dbpedia.extraction.TestExecution
import org.dbpedia.extraction.mappings.SnippetExtractorTest

object SnippetExtractorTestExecution {
  def main(args: Array[String]): Unit = {
    (new SnippetExtractorTest).execute()
  }
}
