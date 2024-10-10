package uk.gov.justice.digital.hmpps.prisonperson.enums

data class DomainEventDetails(
  val type: String,
  val description: String,
)

enum class EventType(
  val telemetryEventName: String? = null,
  val domainEventDetails: DomainEventDetails? = null,
) {
  // NOMIS Physical Attributes - Migration and Sync:
  PHYSICAL_ATTRIBUTES_MIGRATED("prison-person-api-physical-attributes-migrated"),
  PHYSICAL_ATTRIBUTES_SYNCED("prison-person-api-physical-attributes-synced", DomainEventDetails("prison-person.physical-attributes.updated", "The physical attributes of a prisoner have been updated.")),
  PHYSICAL_ATTRIBUTES_SYNCED_HISTORICAL("prison-person-api-physical-attributes-synced-historical"),

  // NOMIS Profile Details -> Physical Attributes - Migration and Sync:
  PROFILE_DETAILS_PHYSICAL_ATTRIBUTES_MIGRATED("prison-person-api-profile-details-physical-attributes-migrated"),
  PROFILE_DETAILS_PHYSICAL_ATTRIBUTES_SYNCED("prison-person-api-profile-details-physical-attributes-synced", DomainEventDetails("prison-person.physical-attributes.updated", "The physical attributes of a prisoner have been updated.")),
  PROFILE_DETAILS_PHYSICAL_ATTRIBUTES_SYNCED_HISTORICAL("prison-person-api-profile-details-physical-attributes-synced-historical"),

  // Physical Attributes - Update:
  PHYSICAL_ATTRIBUTES_UPDATED("prison-person-api-physical-attributes-updated", DomainEventDetails("prison-person.physical-attributes.updated", "The physical attributes of a prisoner have been updated.")),

  // Physical Attributes - Merge (domain event is same as update):
  PHYSICAL_ATTRIBUTES_MERGED("prison-person-api-physical-attributes-merged", DomainEventDetails("prison-person.physical-attributes.updated", "The physical attributes of a prisoner have been updated.")),
}
