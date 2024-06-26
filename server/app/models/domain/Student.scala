package tech.vauldex.tel.models.domain

import java.util.UUID
import java.time.Instant

case class Student(
  id: UUID,
  idNumber: String,
  idHomeroom: Option[UUID],
  email: String,
  name: Name,
  guardianName: Option[String],
  guardianEmail: Option[String],
  avatar: Option[String],
  idGuardian: Option[UUID],
  createdAt: Instant)

object Student {
  val tupled = (apply: (
    UUID,
    String,
    Option[UUID],
    String,
    Name,
    Option[String],
    Option[String],
    Option[String],
    Option[UUID],
    Instant) => Student).tupled

  def apply(
    email: String,
    idNumber: String,
    idHomeroom: Option[UUID] = None,
    name: Name,
    guardianName: Option[String] = None,
    guardianEmail: Option[String] = None,
    idGuardian: Option[UUID] = None): Student =
    apply(UUID.randomUUID, idNumber, idHomeroom, email, name, guardianName, guardianEmail, None, idGuardian, Instant.now)
}
