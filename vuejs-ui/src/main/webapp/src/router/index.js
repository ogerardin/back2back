import Vue from 'vue'
import Router from 'vue-router'

import HelloWorld from '@/components/HelloWorld'
import BackupSets from '@/components/BackupSets'
import Sources from '@/components/Sources'

Vue.use(Router);

export default new Router({
  routes: [
    {
      path: '/',
      name: 'HelloWorld',
      component: HelloWorld
    },
    {
      path: '/backupsets',
      name: 'BackupSets',
      component: BackupSets
    },
    {
      path: '/sources',
      name: 'Sources',
      component: Sources
    },
  ]
})
