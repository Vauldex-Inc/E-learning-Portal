<template>
  <div>
    <tel-notification v-if="message.isError" :error="message.isError">
      {{ $t(message.text) }}
    </tel-notification>
    <tel-signin-form request-link="/request/code" include-guardian-signin @submit="signin" />
  </div>
</template>

<script lang="ts">
import Vue from 'vue'
import Message from '~/types/message'
import TelNotification from '~/components/common/atoms/tel-notification/index.vue'
import TelSigninForm from '~/components/common/organisms/tel-signin-form/index.vue'
import { i18n } from '~/plugins/i18n'

// Message object
const message: Message = {
  isError: false,
  text: ''
}

const isSubmit = false

export default Vue.extend({
  name: 'TelSigninPage',
  middleware: 'unauthenticated',
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
  computed: {
    redirectUri () {
      return this.$route.query.redirect_uri
    }
  },
  methods: {
    async signin (data: { email: string, password: string }) {
      try {
        await this.$store.dispatch('authentication/SIGNIN', data)
        this.$router.push('/my-page')
      } catch (error) {
        this.message.isError = true
        this.message.text = this.checkError((error.response || {}).status)
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
