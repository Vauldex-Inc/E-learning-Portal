import Vue from 'vue'
import { ApplyEvent, CalendarEvent } from '~/types/calendar'
import Message from '~/types/message'
import TelCalendarEvent from '~/components/organisms/tel-calendar-event/index.vue'
import TelNotification from '~/components/common/atoms/tel-notification/index.vue'
import { i18n } from '~/plugins/i18n'

const date = {
  start: '',
  end: ''
}

const screenWidth: number = 0

export default Vue.extend({
  name: 'TelCalendarPage',
  layout: 'auth-level',
  components: {
    TelCalendarEvent,
    TelNotification
  },
  data () {
    return {
      date,
      screenWidth
    }
  },
  computed: {
    fcEvents (): CalendarEvent[] {
      return this.$store.getters['calendar/getEvents']
    },
    getEvent (): CalendarEvent {
      return this.$store.getters['calendar/getEvent']
    },
    message (): Message {
      return this.$store.getters['calendar/getFetchMessage']
    }
  },
  async created () {
    if (this.$route.query.id_event) {
      await this.fetchEvent(this.$route.query.id_event)
    }
  },
  methods: {
    applyEvent (event: ApplyEvent): void {
      this.$store.dispatch('calendar/APPLY_EVENT', event)
    },
    eventClick (event: any) {
      this.fetchEvent(event.data.id)
    },
    changeMonth (data: any) {
      this.date.start = data.start
      this.date.end = data.end
      this.fetchEvents(this.date.start, this.date.end)
    },
    async fetchEvents (startDate: string, endDate: string) {
      await this.$store.dispatch('calendar/FETCH_EVENTS', {
        startDate,
        endDate
      })
    },
    async fetchEvent (idEvent: string): Promise<void> {
      await this.$store.dispatch('calendar/FETCH_EVENT', idEvent)
    }
  },
  head () {
    return {
      title: i18n.tc('main_menu.calendar') + ' - ' + i18n.tc('app.title')
    }
  }
})
