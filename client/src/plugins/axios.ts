import { Plugin } from '@nuxt/types'

declare module 'vuex/types/index' {
  interface Store<S> {
    $api: any
  }
}

const api: Plugin = (ctx: any, inject: any) => {
  const _api: any = ctx.$axios.create({
    headers: { 'Content-Type': 'application/json' },
    withCredentials: true
  })

  _api.setBaseURL(process.env.apiUrl)
  inject('api', _api)
}

export default api
