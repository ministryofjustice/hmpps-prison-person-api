package uk.gov.justice.digital.hmpps.prisonperson.enums

data class DomainEventDetails(
  val type: String,
  val description: String,
)

data class TelemetryEventDetails(
  val name: String,
  val addPrisonerPrisonId: Boolean = false,
)

enum class EventType(
  val telemetryEventDetails: TelemetryEventDetails? = null,
  val domainEventDetails: DomainEventDetails? = null,
) {
  // NOMIS Physical Attributes - Migration and Sync:
  PHYSICAL_ATTRIBUTES_MIGRATED(
    TelemetryEventDetails("prison-person-api-physical-attributes-migrated"),
  ),
  PHYSICAL_ATTRIBUTES_SYNCED(
    TelemetryEventDetails("prison-person-api-physical-attributes-synced", addPrisonerPrisonId = true),
    DomainEventDetails("prison-person.physical-attributes.updated", "The physical attributes of a prisoner have been updated."),
  ),
  PHYSICAL_ATTRIBUTES_SYNCED_HISTORICAL(
    TelemetryEventDetails("prison-person-api-physical-attributes-synced-historical", addPrisonerPrisonId = true),
  ),

  // NOMIS Profile Details -> Physical Attributes - Migration and Sync:
  PROFILE_DETAILS_PHYSICAL_ATTRIBUTES_MIGRATED(
    TelemetryEventDetails("prison-person-api-profile-details-physical-attributes-migrated"),
  ),
  PROFILE_DETAILS_PHYSICAL_ATTRIBUTES_SYNCED(
    TelemetryEventDetails("prison-person-api-profile-details-physical-attributes-synced", addPrisonerPrisonId = true),
    DomainEventDetails("prison-person.physical-attributes.updated", "The physical attributes of a prisoner have been updated."),
  ),
  PROFILE_DETAILS_PHYSICAL_ATTRIBUTES_SYNCED_HISTORICAL(
    TelemetryEventDetails("prison-person-api-profile-details-physical-attributes-synced-historical", addPrisonerPrisonId = true),
  ),

  // Physical Attributes - Update:
  PHYSICAL_ATTRIBUTES_UPDATED(
    TelemetryEventDetails("prison-person-api-physical-attributes-updated"),
    DomainEventDetails("prison-person.physical-attributes.updated", "The physical attributes of a prisoner have been updated."),
  ),

  // Physical Attributes - Merge (domain event is same as update):
  PHYSICAL_ATTRIBUTES_MERGED(
    TelemetryEventDetails("prison-person-api-physical-attributes-merged"),
    DomainEventDetails("prison-person.physical-attributes.updated", "The physical attributes of a prisoner have been updated."),
  ),
}
