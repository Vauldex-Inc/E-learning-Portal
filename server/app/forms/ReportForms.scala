package tech.vauldex.tel.forms

import java.util.UUID
import play.api.data._
import play.api.data.Forms._
import tech.vauldex.tel._
import dtos._
import models.domain.Report
import CustomForms._

object ReportForms {

  lazy val reportFilterForm = Form(tuple(
    "first_name" -> optional(nonEmptyText),
    "last_name" -> optional(nonEmptyText),
    "id_homeroom" -> optional(uuid),
    "marker" -> markerTuple))

  def updateReportForm(studentNumber: String) = Form(
    mapping(
      "student_number" -> ignored(studentNumber),
      "year" -> number,
      "subject" -> nonEmptyText,
      "course" -> nonEmptyText,
      "required_submission" -> number,
      "report1" -> optional(text),
      "report2" -> optional(text),
      "report3" -> optional(text),
      "report4" -> optional(text),
      "report5" -> optional(text),
      "report6" -> optional(text),
      "report7" -> optional(text),
      "report8" -> optional(text),
      "report9" -> optional(text),
      "report10" -> optional(text),
      "report11" -> optional(text),
      "report12" -> optional(text),
      "report13" -> optional(text),
      "report14" -> optional(text),
      "report15" -> optional(text)
    )(Report.apply)(Report.unapply)
  )
}
