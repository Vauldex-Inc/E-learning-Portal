<template>
  <div>
    <tel-notification v-if="message.isError" :error="message.isError">
      {{ $t(message.text) }}
    </tel-notification>
    <tel-signin-form guardian @submit="signin" />
  </div>
</template>

<script lang="ts">
import Vue from 'vue'
import Message from '~/types/message'
import TelNotification from '~/components/common/atoms/tel-notification/index.vue'
import TelSigninForm from '~/components/organisms/tel-guardian-signin-form/index.vue'
import { i18n } from '~/plugins/i18n'

// Message object
const message: Message = {
  isError: false,
  text: ''
}

const isSubmit = false

export default Vue.extend({
  name: 'TelSigninPage',
  middleware: 'guardian-unauthenticated',
  components: {
    TelNotification,
    TelSigninForm
  },
  data () {
    return {
      message,
      isSubmit
    }
  },
  methods: {
    async signin (data: { username: string, password: string }) {
      try {
        await this.$store.dispatch('authentication/SIGNIN_GUARDIAN', data)
        this.$router.push('/guardian')
      } catch (error) {
        this.message.isError = true
        this.message.text = this.checkError(error.response.status)
      }
    },
    checkError (status: number) {
      switch (status) {
        case 400:
          return 'error.auth_fail'
        case 404:
          return 'error.auth_fail'
        default:
          return 'error.contact_support'
      }
    }
  },
  head () {
    return {
      title: i18n.tc('signin.title') + ' - ' + i18n.tc('app.title')
    }
  }
})
</script>
