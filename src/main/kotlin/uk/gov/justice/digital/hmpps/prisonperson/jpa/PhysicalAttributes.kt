package uk.gov.justice.digital.hmpps.prisonperson.jpa

import jakarta.persistence.CascadeType.ALL
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType.LAZY
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapKey
import jakarta.persistence.OneToMany
import org.hibernate.Hibernate
import org.hibernate.annotations.SortNatural
import uk.gov.justice.digital.hmpps.prisonperson.dto.ReferenceDataSimpleDto
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.PhysicalAttributesDto
import uk.gov.justice.digital.hmpps.prisonperson.dto.response.ValueWithMetadata
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.BUILD
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.FACE
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.FACIAL_HAIR
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.HAIR
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.HEIGHT
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.LEFT_EYE_COLOUR
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.RIGHT_EYE_COLOUR
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.SHOE_SIZE
import uk.gov.justice.digital.hmpps.prisonperson.enums.PrisonPersonField.WEIGHT
import uk.gov.justice.digital.hmpps.prisonperson.enums.Source
import uk.gov.justice.digital.hmpps.prisonperson.enums.Source.DPS
import uk.gov.justice.digital.hmpps.prisonperson.mapper.toSimpleDto
import uk.gov.justice.digital.hmpps.prisonperson.service.event.publish.PhysicalAttributesUpdatedEvent
import java.time.ZonedDateTime
import java.util.SortedSet
import kotlin.reflect.KMutableProperty0

@Entity
class PhysicalAttributes(
  @Id
  @Column(name = "prisoner_number", updatable = false, nullable = false)
  override val prisonerNumber: String,

  @Column(name = "height_cm")
  var height: Int? = null,

  @Column(name = "weight_kg")
  var weight: Int? = null,

  @ManyToOne
  @JoinColumn(name = "hair", referencedColumnName = "id")
  var hair: ReferenceDataCode? = null,

  @ManyToOne
  @JoinColumn(name = "facial_hair", referencedColumnName = "id")
  var facialHair: ReferenceDataCode? = null,

  @ManyToOne
  @JoinColumn(name = "face", referencedColumnName = "id")
  var face: ReferenceDataCode? = null,

  @ManyToOne
  @JoinColumn(name = "build", referencedColumnName = "id")
  var build: ReferenceDataCode? = null,

  @ManyToOne
  @JoinColumn(name = "left_eye_colour", referencedColumnName = "id")
  var leftEyeColour: ReferenceDataCode? = null,

  @ManyToOne
  @JoinColumn(name = "right_eye_colour", referencedColumnName = "id")
  var rightEyeColour: ReferenceDataCode? = null,

  @Column(name = "shoe_size")
  var shoeSize: String? = null,

  // Stores snapshots of each update to a prisoner's physical attributes
  @OneToMany(mappedBy = "prisonerNumber", fetch = LAZY, cascade = [ALL], orphanRemoval = true)
  @SortNatural
  override val fieldHistory: SortedSet<FieldHistory> = sortedSetOf(),

  // Stores timestamps of when each individual field was changed
  @OneToMany(mappedBy = "prisonerNumber", fetch = LAZY, cascade = [ALL], orphanRemoval = true)
  @MapKey(name = "field")
  override val fieldMetadata: MutableMap<PrisonPersonField, FieldMetadata> = mutableMapOf(),

) : WithFieldHistory<PhysicalAttributes>() {

  override fun fieldAccessors(): Map<PrisonPersonField, KMutableProperty0<*>> = mapOf(
    HEIGHT to ::height,
    WEIGHT to ::weight,
    HAIR to ::hair,
    FACIAL_HAIR to ::facialHair,
    FACE to ::face,
    BUILD to ::build,
    LEFT_EYE_COLOUR to ::leftEyeColour,
    RIGHT_EYE_COLOUR to ::rightEyeColour,
    SHOE_SIZE to ::shoeSize,
  )

  @Suppress("UNCHECKED_CAST")
  fun <T> set(field: PrisonPersonField, value: T) {
    (fieldAccessors()[field] as KMutableProperty0<T>).set(value)
  }

  fun toDto(): PhysicalAttributesDto =
    PhysicalAttributesDto(
      height = getValueWithMetadata(::height, HEIGHT),
      weight = getValueWithMetadata(::weight, WEIGHT),
      hair = getRefDataValueWithMetadata(::hair, HAIR),
      facialHair = getRefDataValueWithMetadata(::facialHair, FACIAL_HAIR),
      face = getRefDataValueWithMetadata(::face, FACE),
      build = getRefDataValueWithMetadata(::build, BUILD),
      leftEyeColour = getRefDataValueWithMetadata(::leftEyeColour, LEFT_EYE_COLOUR),
      rightEyeColour = getRefDataValueWithMetadata(::rightEyeColour, RIGHT_EYE_COLOUR),
      shoeSize = getValueWithMetadata(::shoeSize, SHOE_SIZE),
    )

  private fun <T> getValueWithMetadata(value: KMutableProperty0<T>, field: PrisonPersonField) =
    fieldMetadata[field]?.let {
      ValueWithMetadata(
        value.get(),
        it.lastModifiedAt,
        it.lastModifiedBy,
      )
    }

  private fun getRefDataValueWithMetadata(
    value: KMutableProperty0<ReferenceDataCode?>,
    field: PrisonPersonField,
  ): ValueWithMetadata<ReferenceDataSimpleDto?>? =
    fieldMetadata[field]?.let {
      ValueWithMetadata(
        value.get()?.toSimpleDto(),
        it.lastModifiedAt,
        it.lastModifiedBy,
      )
    }

  override fun updateFieldHistory(
    lastModifiedAt: ZonedDateTime,
    lastModifiedBy: String,
  ): Collection<PrisonPersonField> = updateFieldHistory(lastModifiedAt, null, lastModifiedAt, lastModifiedBy, DPS, allFields)

  override fun publishUpdateEvent(source: Source, now: ZonedDateTime, fields: Collection<PrisonPersonField>) {
    registerEvent(
      PhysicalAttributesUpdatedEvent(
        prisonerNumber = prisonerNumber,
        occurredAt = now,
        source = source,
        fields = fields,
      ),
    )
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false

    other as PhysicalAttributes

    if (prisonerNumber != other.prisonerNumber) return false
    if (height != other.height) return false
    if (weight != other.weight) return false
    if (hair != other.hair) return false
    if (facialHair != other.facialHair) return false
    if (face != other.face) return false
    if (build != other.build) return false
    if (leftEyeColour != other.leftEyeColour) return false
    if (rightEyeColour != other.rightEyeColour) return false
    if (shoeSize != other.shoeSize) return false

    return true
  }

  override fun hashCode(): Int {
    var result = prisonerNumber.hashCode()
    result = 31 * result + (height ?: 0)
    result = 31 * result + (weight ?: 0)
    result = 31 * result + (hair?.id.hashCode())
    result = 31 * result + (facialHair?.id.hashCode())
    result = 31 * result + (face?.id.hashCode())
    result = 31 * result + (build?.id.hashCode())
    result = 31 * result + (leftEyeColour?.id.hashCode())
    result = 31 * result + (rightEyeColour?.id.hashCode())
    result = 31 * result + (shoeSize.hashCode())
    return result
  }

  companion object {
    val allFields = listOf(
      HEIGHT,
      WEIGHT,
      HAIR,
      FACIAL_HAIR,
      FACE,
      BUILD,
      LEFT_EYE_COLOUR,
      RIGHT_EYE_COLOUR,
      SHOE_SIZE,
    )
  }
}
