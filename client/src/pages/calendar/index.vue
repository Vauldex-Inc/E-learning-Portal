<template>
  <div>
    <tel-notification
      v-if="message.text"
      :error="message.isError"
      :success="!message.isError"
    >
      {{ $t(message.text) }}
    </tel-notification>
    <tel-calendar-event
      :events="fcEvents"
      :event="getEvent"
      :is-modal-open-on-create="$route.query.id_event !== undefined"
      @eventClick="eventClick"
      @changeMonth="changeMonth"
      @submit="applyEvent"
    />
  </div>
</template>

<script>
import TelCalendarPage from './calendar'

export default TelCalendarPage
</script>
