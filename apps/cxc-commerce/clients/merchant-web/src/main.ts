import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import '@forest/ui-kit/theme/foundation.css'
import { applyTheme } from '@forest/ui-kit/theme'
import { appConfig } from './app.config'

applyTheme(appConfig.themeId)

createApp(App)
  .use(router)
  .mount('#app')
