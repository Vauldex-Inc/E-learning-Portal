package tech.vauldex.tel.models.service

import java.util.UUID
import java.time.Instant
import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import cats.data.{ EitherT, OptionT }
import cats.implicits._
import tech.vauldex.tel._
import errors.TelError
import TelError._
import models.domain._
import models.dao._
import models.repo._
import services._

@Singleton
class HomeroomService @Inject()(
    protected val s3Service: S3Service,
    protected val homeroomVideoRepo: HomeroomVideoRepo,
    protected val homeroomVideoHistoryRepo: HomeroomVideoHistoryRepo,
    protected val homeroomVideoHistoryDAO: HomeroomVideoHistoryDAO,
    protected val homeroomDAO: HomeroomDAO,
    protected val homeroomTeacherDAO: HomeroomTeacherDAO,
    protected val studentDAO: StudentDAO,
    protected val preferenceService: PreferenceService,
    protected val dbConfigProvider: DatabaseConfigProvider,
    implicit val ec: ExecutionContext)
  extends HasDatabaseConfigProvider[db.PostgresProfile] {
  import profile.api._

  lazy val bucketVideo = preferenceService.predef.bucketVideo.synchronized.get
  lazy val bucketCf = preferenceService.predef.bucketCf.synchronized.get

  def createHomeroomVideoHistory(history: HomeroomVideoHistory)
    : EitherT[Future, TelError, Instant] = {
    homeroomVideoRepo
      .find(history.idHomeroom, history.videoCreatedAt)
      .toRight[TelError](HomeroomVideoNotExists())
      .flatMap { _ =>
        EitherT {
          db.run(homeroomVideoHistoryDAO.query += history).map { num =>
            if (num == 1) Right(history.startedAt)
            else Left(InternalContradictionError())
          }
        }
      }
    }

  def finishHomeroomVideoHistory(
      idStudent: UUID,
      idHomeroom: UUID,
      videoCreatedAt: Instant,
      startedAt: Instant): EitherT[Future, TelError, Unit] =  {
    homeroomVideoHistoryRepo.find(idStudent, idHomeroom, videoCreatedAt, startedAt)
      .toRight(HomeroomVideoHistoryNotExists())
      .flatMap { sh =>
        EitherT {
          homeroomVideoHistoryRepo
            .updateHomeroomVideoHistory(
              idStudent,
              idHomeroom,
              videoCreatedAt,
              startedAt,
              Some(Instant.now)).map { result =>
              if (result === 1) Right(())
              else Left(InternalContradictionError())
            }
        }
      }
    }

  def findStudentTeacherHomeroom(idStudent: UUID, idTeacher: UUID)
    : OptionT[Future, (Student, Homeroom, HomeroomTeacher)] = OptionT {
    db.run(studentDAO.query(idStudent)
      .join(homeroomDAO.query)
      .on(_.idHomeroom === _.id)
      .join(homeroomTeacherDAO.query.filter(_.idTeacher === idTeacher))
      .on {
        case ((student, homeroom), homeroomTeacher) =>
          student.idHomeroom === homeroom.id && student.idHomeroom === homeroomTeacher.idHomeroom &&
          homeroom.id === homeroomTeacher.idHomeroom
      }.map { case ((student, homeroom), homeroomTeacher) =>
        (student.student, homeroom, homeroomTeacher)
      }.result.headOption)
  }

  def deleteHomeroomVideo(idHomeroom: UUID, videoCreatedAt: Instant): EitherT[Future, TelError, Unit] = {
     homeroomVideoRepo.find(idHomeroom, videoCreatedAt)
      .toRight(HomeroomVideoNotExists())
      .flatMap { hv =>
        EitherT {
          db.run(homeroomVideoHistoryDAO.query(idHomeroom, videoCreatedAt).result)
            .flatMap { res =>
              if (res.isEmpty) {
                homeroomVideoRepo.delete(idHomeroom, videoCreatedAt).map { r =>
                  if (r === 1) {
                    doDeleteHomeroomVideo(hv)
                    Right(())
                  } else Left(InternalContradictionError())
                }
              } else Future.successful(Left(HomeroomVideoHistoryExists()))
          }
        }
      }
  }

  def forceDeleteHomeroomVideo(idHomeroom: UUID, videoCreatedAt: Instant): EitherT[Future, TelError, Unit] = {
    homeroomVideoRepo.find(idHomeroom, videoCreatedAt)
      .toRight(HomeroomVideoNotExists())
      .flatMap { hv =>
        EitherT {
          homeroomVideoRepo.delete(idHomeroom, videoCreatedAt).map { r =>
            if (r === 1) {
              doDeleteHomeroomVideo(hv)
              Right(())
            } else Left(InternalContradictionError())
          }
        }
      }
  }

  private def doDeleteHomeroomVideo(homeroomVideo: HomeroomVideo)
    : (Option[Future[Option[akka.Done]]],
      Option[Future[Option[akka.Done]]],
      Option[Future[Option[akka.Done]]]) = {
    val url = homeroomVideo.url(bucketVideo).map(s3Service.delete(_))
    val distUrl = homeroomVideo.url(bucketCf).map(s3Service.delete(_))
    val thumbnail = homeroomVideo.thumbnail.map(s3Service.delete(_))
    (url, distUrl, thumbnail)
  }
}
