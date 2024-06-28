package tech.vauldex.tel.models.domain

import java.time.Instant
import java.util.UUID

case class Homeroom(id: UUID, year: Int, grade: String, section: String, createdAt: Instant)

object Homeroom {
  val MAX_SECTION_LENGTH: Int = 1

  val tupled = (apply: (UUID, Int, String, String, Instant) => Homeroom).tupled
  def apply(year: Int, grade: String, section: String): Homeroom =
    Homeroom(UUID.randomUUID, year, grade, section, Instant.now)
}
