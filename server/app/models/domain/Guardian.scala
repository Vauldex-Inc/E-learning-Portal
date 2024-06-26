package tech.vauldex.tel.models.domain

import java.util.UUID
import java.time.Instant

case class Guardian(
  id: UUID,
  username: String,
  password: String,
  createdAt: Instant)