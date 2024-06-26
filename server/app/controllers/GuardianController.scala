package tech.vauldex.tel.controllers

import java.util.UUID
import java.time.Instant
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
import forms.UserForms._
import models.mapper.JsonSerializers._
import models.domain._
import models.repo._
import models.service._
import models.mapper.JsonSerializers._
import dtos._
import errors._
import security._
import services._


@Singleton
class GuardianController @Inject()(
    protected val secureGuardianAction: SecureGuardianAction,
    protected val secureTeacherAction: SecureTeacherAction,
    protected val guardianService: GuardianService,
    protected val studentService: StudentService,
    protected val guardianRepo: GuardianRepo,
    protected val s3Service: S3Service,
    implicit val ec: ExecutionContext,
    val controllerComponents: ControllerComponents)
  extends BaseController
  with play.api.i18n.I18nSupport {
  
  def create = secureTeacherAction.async { implicit request =>
    addGuardianForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(formWithErrors.errorsAsJson)),
      { case (username, password, idStudent) =>
        guardianService.create(Guardian(UUID.randomUUID, username, password, Instant.now), idStudent)
          .fold(ServiceErrorHandler(_), guardian => Created(Json.toJson(guardian)))
      }
    )
  }

  def getGuardian(id: UUID) = secureTeacherAction.async { implicit request =>
    guardianService.getGuardian(id)
      .map(guardian => Ok(Json.toJson(guardian))).getOrElse(NotFound)
  }

  def getMine = secureGuardianAction.async { implicit request =>
    guardianRepo.find(request.guardian.id)
      .map(guardian => Ok(Json.toJson(guardian)))
      .getOrElse(NotFound)
  }

  def getStudents = secureGuardianAction.async { implicit request =>
    studentService.getStudentByIdGuardian(request.guardian.id)
      .map(student => Ok(studentJson(student, s3Service)))
      .getOrElse(NotFound)
  }

  def login = Action.async { implicit request =>
    guardianLoginForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(formWithErrors.errorsAsJson)),
      { case (username, password) =>
        guardianRepo.findByUsernameAndPassword(username, password)
          .map(guardian => Ok(Json.toJson(guardian)).addingToSession(secureGuardianAction.key -> guardian.id.toString))
          .getOrElse(Unauthorized)
      })
  }

   def logout = Action { implicit request =>
    NoContent.removingFromSession(secureGuardianAction.key)
  }
}