<template>
  <section class="my-page">
    <div>
      <tel-banner
        :name="student.name"
        :avatar="student.avatar"
        :tag="`${student.grade} - ${student.class}`"
        :summary=" { label: 'student.average_view', value: progress.reviews }"
        :progress="progressObj"
        :locale="locale"
      />
    </div>
    <div>
      <tel-heading :heading-text="$t('announcements.title')">
        <template #link>
          <nuxt-link to="/announcements">
            {{ $t('common.see_all') }}
          </nuxt-link>
        </template>
      </tel-heading>
      <tel-announcements
        :announcements="announcements"
        :item-limit="5"
        @modal="showAnnouncement"
      />
    </div>
    <div>
      <tel-heading :heading-text="$t('common.lets_start')" />
      <tel-session-dashboard />
    </div>
    <tel-announcement-modal
      :is-show="modal.isShow"
      :announcement="modal.announcement"
      @close:modal="closeModal"
      @mark-read="markRead"
    />
  </section>
</template>

<script lang="ts">
import Vue from 'vue'
import { mapGetters, mapActions } from 'vuex'
import Announcement from '~/types/announcement'
import TelBanner from '~/components/common/organisms/tel-banner/index.vue'
import TelAnnouncements from '~/components/common/organisms/tel-announcements-list/index.vue'
import TelHeading from '~/components/common/molecules/tel-heading/index.vue'
import TelSessionDashboard from '~/components/organisms/tel-session-dashboard/index.vue'
import TelAnnouncementModal from '~/components/common/organisms/tel-announcement-modal/index.vue'
import { i18n } from '~/plugins/i18n'

export default Vue.extend({
  name: 'TelMyPage',
  middleware: 'authenticated',
  layout: 'auth-level',
  components: {
    TelBanner,
    TelAnnouncements,
    TelHeading,
    TelSessionDashboard,
    TelAnnouncementModal
  },
  data () {
    return {
      modal: {
        isShow: false,
        announcement: {}
      }
    }
  },
  computed: {
    ...mapGetters({
      student: 'student/student',
      progress: 'student/progress',
      announcements: 'announcement/all',
      isEmpty: 'announcement/isEmpty',
      locale: 'i18n/getLocale'
    }),
    progressObj () {
      return {
        label: 'student.session_progress',
        dividend: this.progress.watched,
        divisor: this.progress.videos,
        percentage: this.percentage
      }
    },
    percentage () {
      return Math.round((this.progress.watched / this.progress.videos) * 100) || 0
    }
  },
  created () {
    this.setStudent()
    this.setProgress()
    this.setAnnouncements()
  },
  methods: {
    ...mapActions({
      setStudent: 'student/SET_STUDENT',
      setProgress: 'student/SET_PROGRESS',
      setAnnouncements: 'announcement/SET_ANNOUNCEMENTS',
      markAnnouncementRead: 'announcement/MARK_READ'
    }),
    closeModal () {
      this.modal.isShow = false
    },
    showAnnouncement (announcement: Announcement) {
      this.modal.announcement = announcement
      this.modal.isShow = true
    },
    async markRead (id: string) {
      await this.markAnnouncementRead(id)
      this.modal.isShow = false
    }
  },
  head () {
    return {
      title: i18n.tc('my_page') + ' - ' + i18n.tc('app.title')
    }
  }
})
</script>
