package uk.gov.justice.digital.hmpps.prisonperson.jpa

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.hibernate.Hibernate
import org.springframework.data.domain.AbstractAggregateRoot
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.HealthDto
import uk.gov.justice.digital.hmpps.prisonperson.mapper.toDto

@Entity
class Health(
  @Id
  @Column(name = "prisoner_number", updatable = false, nullable = false)
  val prisonerNumber: String,

  @ManyToOne
  @JoinColumn(name = "smoker_or_vaper", referencedColumnName = "id")
  var smokerOrVaper: ReferenceDataCode? = null,
) : AbstractAggregateRoot<Health>() {

  fun toDto(): HealthDto = HealthDto(
    smokerOrVaper?.toDto(),
  )

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false

    other as Health

    if (prisonerNumber != other.prisonerNumber) return false
    if (smokerOrVaper != other.smokerOrVaper) return false

    return true
  }

  override fun hashCode(): Int {
    var result = prisonerNumber.hashCode()
    result = 31 * result + smokerOrVaper.hashCode()
    return result
  }
}
