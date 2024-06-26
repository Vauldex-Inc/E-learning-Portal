package tech.vauldex.tel.controllers

import java.util.UUID
import java.time.LocalDate
import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }
import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.data._
import play.api.data.Forms._
import cats.implicits._
import tech.vauldex.tel._
import errors.ServiceErrorHandler
import forms.AttendanceForms._
import forms.CustomForms._
import models.domain._
import models.repo._
import models.service._
import models.mapper.JsonSerializers._
import security._

@Singleton
class AttendanceController @Inject()(
    secureStudentAction: SecureStudentAction,
    secureTeacherAction: SecureTeacherAction,
    attendanceRepo: AttendanceRepo,
    studentService: StudentService,
    implicit val ec: ExecutionContext,
    val controllerComponents: ControllerComponents)
  extends BaseController
  with play.api.i18n.I18nSupport {

  def createAttendance = secureStudentAction.async { implicit request =>
    createAttendanceForm(request.student.id).bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(formWithErrors.errorsAsJson)),
      attendance => {
        attendanceRepo.find(attendance.idStudent, attendance.appliedAt, attendance.`type`).flatMap {
          case Some(a) => Future.successful(Conflict)
          case None => {
            attendanceRepo.add(attendance.copy(
              arrivalTime = if (attendance.`type` == Attendances.Absent) None
              else attendance.arrivalTime))
              .map { count =>
                if (count == 1) Ok
                else if (count == 0) BadRequest
                else Conflict
              }
          }
        }
      }
    )
  }

  def getAttendance = secureTeacherAction.async { implicit request  =>
    attendanceForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(formWithErrors.errorsAsJson)),
      { case (idHomeroom, marker, start, end, isRead) =>
        studentService.findAttendance(marker, request.teacher.id, idHomeroom, start, end, isRead)
        .map { page =>
          Ok(page.copy(results = page.results.map {
              studentAttendanceJson(_)
          }).toJson)
        }
      }
    )
  }

  def countUnreadAttendance = secureTeacherAction.async { implicit request =>
    studentService.countUnreadAttendances(request.teacher.id)
      .map(count => Ok(Json.toJson(count)))
  }

  def getMyAttendance = secureStudentAction.async { implicit request =>
    markerForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(formWithErrors.errorsAsJson)),
      marker =>
        attendanceRepo.find(request.student.id, marker)
          .map(pages => Ok(pages.toJson))
    )
  }

  def setAttendanceAsRead(idStudent: UUID) =
    secureTeacherAction.async { implicit request =>
      appliedAtForm.bindFromRequest.fold(
        formWithErrors => Future.successful(BadRequest(formWithErrors.errorsAsJson)),
        { case (appliedAt, typ) =>
      attendanceRepo.setAsRead(idStudent, appliedAt, typ).map { count =>
        if (count == 1) NoContent
        else InternalServerError
      } recover {
          case e => InternalServerError(e.getMessage)
      }
    })
  }

  def filterRead(read: Boolean) = secureTeacherAction.async { implicit request =>
    markerForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(formWithErrors.errorsAsJson)),
      marker => studentService.filterRead(request.teacher.id, marker, read)
        .map { page =>
          Ok(page.copy(results = page.results.map(studentAttendanceJson(_))).toJson)
        })
  }
}
