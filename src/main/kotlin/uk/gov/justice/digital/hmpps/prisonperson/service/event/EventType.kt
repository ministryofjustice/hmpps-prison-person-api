package uk.gov.justice.digital.hmpps.prisonperson.service.event

enum class EventType(
  val domainEventType: String,
  val telemetryEventName: String,
  val description: String,
) {
  PHYSICAL_ATTRIBUTES_UPDATED("prison-person.physical-attributes.updated", "PrisonPersonPhysicalAttributesUpdate", "The physical attributes of a prisoner has been updated."),
}
