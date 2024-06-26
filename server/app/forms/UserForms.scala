package tech.vauldex.tel.forms

import java.time.Instant
import java.util.UUID
import play.api.data._
import play.api.data.Forms._
import tech.vauldex.tel._
import models.domain._
import dtos._
import CustomForms._

object UserForms {

  lazy val nameMapping = mapping(
    "first" -> nonEmptyText(maxLength = 200),
    "last" -> nonEmptyText(maxLength = 200))(Name.apply)(Name.unapply)

  lazy val addressMapping = mapping(
    "line1" -> optional(text(maxLength = Address.MAX_LINE1_LENGTH)),
    "line2" -> optional(text(maxLength = Address.MAX_LINE2_LENGTH)),
    "postal_code" -> optional(text(maxLength = Address.MAX_POSTAL_CODE_LENGTH)))(Address.apply)(Address.unapply)

  lazy val updateTeacherForm = Form(tuple(
    "name" -> optional(nameMapping),
    "address" -> optional(addressMapping),
    "majors" -> optional(list(text)),
    "role" -> optional(roleMapping)
  ))

  lazy val roleMapping = nonEmptyText.transform(
    role => Roles.withName(role),
    { role: Roles.Value =>  role.toString} )

  lazy val addTeacherForm = Form(mapping(
    "email" -> email,
    "name" -> nameMapping,
    "address" -> addressMapping,
    "majors" -> list(text),
    "role" -> roleMapping)(CreateTeacherDTO.apply)(CreateTeacherDTO.unapply))

  lazy val addStudentForm = Form(mapping(
    "email" -> email,
    "id_number" -> nonEmptyText,
    "id_homeroom" -> optional(uuid),
    "name" -> nameMapping
  )(CreateStudentDTO.apply)(CreateStudentDTO.unapply))

  lazy val loginForm = Form(mapping(
    "email" -> email,
    "password" -> studentPasswordText)(LoginDTO.apply)(LoginDTO.unapply))

  lazy val updatePasswordForm = Form(tuple(
    "password" -> studentPasswordText,
    "new_password" -> matching(
      "main" -> studentPasswordText,
      "confirm" -> studentPasswordText)))

  lazy val advisoryForm = Form(tuple(
    "id_teacher" -> uuid,
    "student_list" -> idList))

  lazy val guardianForm = Form(tuple(
    "name" -> optional(optional(text)),
    "email" -> optional(optional(text))))

  lazy val emailForm = Form("email" -> email)

  lazy val codeForm = Form("code" -> text)

  lazy val resetPasswordForm = Form(tuple(
    "email" -> email,
    "password" -> matching(
      "main" -> studentPasswordText,
      "confirm" -> studentPasswordText),
    "code" -> nonEmptyDigit))

  lazy val requestPasswordForm = Form(tuple(
    "email" -> email,
    "last_name" -> nonEmptyText))

  lazy val studentSearchForm = Form(tuple(
    "marker" -> markerTuple,
    "first_name" -> optional(text),
    "last_name" -> optional(text),
    "year" -> optional(number),
    "grade" -> optional(text),
    "section" -> optional(text),
    "id_teacher" -> optional(uuid)
  ))

  lazy val teacherSearchForm = Form(tuple(
    "marker" -> markerTuple,
    "first_name" -> optional(text),
    "last_name" -> optional(text)
  ))

  lazy val updateStudentForm = Form(tuple(
    "name" -> nameMapping,
    "id_homeroom" -> uuid,
    "id_number" -> nonEmptyText
  ))

  lazy val majorForm = Form("name" -> nonEmptyText)

  lazy val addGuardianForm = Form(tuple(
    "username" -> usernameText(caseInsensitive = false),
    "password" -> guardianPasswordText,
    "id_student" -> optional(uuid)))
  
  lazy val guardianLoginForm = Form(tuple(
    "username" -> usernameText(caseInsensitive = false),
    "password" -> guardianPasswordText))
}

