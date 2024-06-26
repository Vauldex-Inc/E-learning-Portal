import { Context } from '@nuxt/types/app'
import { unauthorized } from '~/utils/unauthorized'

export default async function (ctx: Context) {
  const { store } = ctx

  try {
    await store.dispatch('student/SET_STUDENT')
  } catch (error) {
    if (error.statusCode === 401 || error.statusCode === 403) {
      unauthorized(ctx)
    }
  }
}
