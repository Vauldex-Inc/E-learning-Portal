import { ActionTree } from 'vuex'
import { handleError } from '~/utils/errors'

interface StateAccount {
  email: string;
  password: string;
}

export const state = (): StateAccount => ({
  email: '',
  password: ''
})

export const actions: ActionTree<StateAccount, any> = {
  /**
  * Sign in Student account via email and password.
  * When fail it will return error.
  */
  SIGNIN (_, payload: { email: string, password: string }) {
    try {
      return this.$api.post('/students/login', payload)
    } catch (error) {
      return handleError(error)
    }
  },
  SIGNIN_GUARDIAN (_, payload: { username: string, password: string }) {
    try {
      return this.$api.post('/guardians/login', payload)
    } catch (error) {
      return handleError(error)
    }
  },
  async SIGNOUT ({ commit }) {
    try {
      await this.$api.delete('/students/logout')
      commit('UNSET_TOKEN', null, { root: true }) // Unset the token. UNSET_TOKEN came from index.ts
    } catch (error) {
      return handleError(error)
    }
  },
  async SIGNOUT_GUARDIAN ({ commit }) {
    try {
      await this.$api.delete('/guardians/logout')
      commit('UNSET_TOKEN', null, { root: true }) // Unset the token. UNSET_TOKEN came from index.ts
    } catch (error) {
      return handleError(error)
    }
  }
}
