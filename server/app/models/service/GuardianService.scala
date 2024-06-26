package tech.vauldex.tel.models.service

import java.time._
import java.util.UUID
import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }
import play.api.i18n._
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import cats.data.{ EitherT, OptionT }
import cats.implicits._
import tech.vauldex.tel._
import errors.TelError
import TelError._
import models.domain._
import models.dao._
import models.repo._

@Singleton
class GuardianService @Inject()(
    protected val studentDAO: StudentDAO,
    protected val guardianDAO: GuardianDAO,
    protected val studentRepo: StudentRepo,
    protected val guardianRepo: GuardianRepo,
    implicit val messagesApi: MessagesApi,
    protected val dbConfigProvider: DatabaseConfigProvider,
    implicit val ec: ExecutionContext)
  extends HasDatabaseConfigProvider[db.PostgresProfile]
  with I18nSupport {
  import profile.api._

  def create(guardian: Guardian, idStudent: Option[UUID]): EitherT[Future, TelError, Guardian] = {
    for {
      _ <- guardianRepo.validateUsername(guardian.username)
      result <- EitherT {
        guardianRepo
          .add(guardian)
          .map(num => Either.cond(num == 1, guardian, InternalContradictionError(): TelError))
      }
      _ <- linkGuardianToStudent(Some(guardian.id), idStudent)

    } yield result
  }

  private def linkGuardianToStudent(idGuardian: Option[UUID], idStudent: Option[UUID]): EitherT[Future, TelError, Unit] = {
    idStudent.map(id => EitherT[Future, TelError, Unit] {
        studentRepo.updateIdGuardian(id, idGuardian)
          .map(num => if (num == 1) Right(()) else Left(InternalContradictionError()))
      }).getOrElse(EitherT.right[TelError](Future.successful(())))
  }

  def getGuardian(idStudent: UUID): OptionT[Future, Guardian] = {
    OptionT(db.run(studentDAO.query(idStudent)
      .join(guardianDAO.query)
      .on((student, guardian) => student.idGuardian === guardian.id).map{
        case (student, guardian) => guardian
      }.result.headOption))
  }
}