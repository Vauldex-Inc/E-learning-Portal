package tech.vauldex.tel.models.service

import java.time._
import java.util.UUID
import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }
import play.api.i18n._
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import cats.data.EitherT
import cats.implicits._
import com.github.tminglei.slickpg.window.PgWindowFuncSupport.WindowFunctions._
import tech.vauldex.tel._
import errors.TelError
import TelError._
import models.domain._
import models.dao._
import models.repo._
import models.service._
import models.MaybeFilter._
import services.Page

@Singleton
class ReportService @Inject() (
    protected val studentDAO: StudentDAO,
    protected val reportDAO: ReportDAO,
    protected val homeroomDAO: HomeroomDAO,
    protected val reportRepo: ReportRepo,
    protected val studentService: StudentService,
    protected val dbConfigProvider: DatabaseConfigProvider,
    implicit val messagesApi: MessagesApi,
    implicit val ec: ExecutionContext
) extends HasDatabaseConfigProvider[db.PostgresProfile]
    with I18nSupport {
  import profile.api._

  def get(idNumber: String): Future[Seq[Report]] = {
    db.run(
      studentDAO.query
        .filter(_.idNumber === idNumber)
        .join(homeroomDAO.query)
        .on { case (student, homeroom) =>
          student.idHomeroom === homeroom.id
        }
        .join(reportDAO.query.filter(_.studentNumber === idNumber))
        .on { case ((student, homeroom), report) =>
          student.idNumber === report.studentNumber && homeroom.year === report.year
        }
        .map(_._2)
        .result
    )
  }

  def getStudentReport(
      idNumber: String
  ): Future[(Option[((Student, Security), Option[Homeroom])], Seq[Report])] = {
    for {
      student <- studentService.findByIdNumber(idNumber)
      reports <- get(idNumber)
    } yield {
      (student, reports)
    }
  }

  def all(
      firstName: Option[String],
      lastName: Option[String],
      idHomeroom: Option[UUID],
      marker: (Long, Long)
  ) = {
    val q = studentDAO.query
      .filterBy(firstName.map(_.toLowerCase))(v => d => d.firstName.toLowerCase like s"%$v%")
      .filterBy(lastName.map(_.toLowerCase))(v => d => d.lastName.toLowerCase like s"%$v%")
      .query
      .join(
        homeroomDAO.query
          .filterBy(idHomeroom)(v => d => d.id === v)
          .query
      )
      .on { case (student, homeroom) =>
        student.idHomeroom === homeroom.id
      }
      .join(reportDAO.query)
      .on { case ((student, homeroom), report) =>
        student.idNumber === report.studentNumber && homeroom.year === report.year
      }
      .map { case ((student, homeroom), report) =>
        (student, homeroom)
      }
      .distinct
    val offset = marker._1 - 1
    val limit = marker._2 - marker._1 + 1
    val paged = q.sortBy(_._1.createdAt.desc).drop(offset).take(limit)
    for {
      results <- db.run(paged.result)
      total <- db.run(q.length.result)
    } yield Page(results, marker, total)
  }
}
