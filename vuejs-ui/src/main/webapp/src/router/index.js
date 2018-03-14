import Vue from 'vue'
import Router from 'vue-router'

import HelloWorld from '@/components/HelloWorld'
import BackupSets from '@/components/BackupSets'
import BackupSetList from '@/components/BackupSetList'
import BackupSetFiles from '@/components/BackupSetFiles'
import Sources from '@/components/Sources'
import SourceList from '@/components/SourceList'
import SourceDetails from '@/components/SourceDetails'
import SourceEdit from '@/components/SourceEdit'

Vue.use(Router);

export default new Router({
  routes: [
    {
      path: '/',
      component: HelloWorld
    },
    {
      path: '/backupsets',
      component: BackupSets,
      children: [
        {path: '', component: BackupSetList},
        {path: ':id/files', name:'backupset-files', component: BackupSetFiles}
      ]
    },
    {
      path: '/sources',
      component: Sources,
      children: [
        { path: '', component: SourceList },
        // { path: 'add', name: 'source-add', component: SourceAdd },
        { path: ':id', name: 'source-details', component: SourceDetails },
        { path: ':id/edit', name: 'source-edit', component: SourceEdit },
      ]
    },
  ]
})
