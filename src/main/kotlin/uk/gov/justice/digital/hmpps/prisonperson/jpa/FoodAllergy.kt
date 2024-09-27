package uk.gov.justice.digital.hmpps.prisonperson.jpa

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.Hibernate
import java.io.Serializable

@Embeddable
class FoodAllergyId(
  @Column(name = "prisoner_number", updatable = false, nullable = false)
  val prisonerNumber: String,

  @ManyToOne
  @JoinColumn(name = "allergy", referencedColumnName = "id")
  var allergy: ReferenceDataCode,
) : Serializable

@Entity
@Table(name = "food_allergies")
class FoodAllergy(
  @EmbeddedId
  val id: FoodAllergyId,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false

    other as FoodAllergy

    if (id.prisonerNumber != other.id.prisonerNumber) return false
    if (id.allergy != other.id.allergy) return false

    return true
  }

  override fun hashCode(): Int {
    return 31 * id.hashCode()
  }
}
