package controllers

import models._
import Utils._
import play.api.mvc.{ SimpleResult, Controller, ResponseHeader }
import play.api.libs.iteratee.Enumerator
import play.api.templates.Html

object FileServing extends Controller {
  def apply(id: IdType) = SlickAction(AuthorOrNCReviewer(id)) {
      implicit r =>
    val file = Files.withId(Id[File](id))
    val headers =
      if(file.name endsWith ".pdf") Map(
        CONTENT_TYPE -> "application/pdf",
        CONTENT_DISPOSITION -> s"inline; filename=${file.name}")
      else Map(
        CONTENT_TYPE -> "application/octet-stream",
        CONTENT_DISPOSITION -> s"attachment; filename=${file.name}")
    SimpleResult(
      header=ResponseHeader(200, headers),
      body=Enumerator(file.content)
    )
  }

  def linkToFile(file: File): Html = {
    /** Source: http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java */
    def humanReadableByteCount(size: Long): String = {
      import Math._
      val unit = 1000
      if (size < unit)
         size + " B"
      else {
        val exp = (log(size) / log(unit)).toInt
        val pre = "kMGTPE" charAt (exp - 1)
        "%.1f %sB" format (size / pow(unit, exp), pre)
      }
    }
    Html(s"""
    <a href="${routes.FileServing(file.id.value)}">
    ${file.name} (${humanReadableByteCount(file.size)})</a>
    """)
  }
}