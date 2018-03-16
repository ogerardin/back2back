import Vue from 'vue'
import Router from 'vue-router'

import HomeView from '@/components/HomeView'
import BackupSets from '@/components/BackupSets'
import BackupSetList from '@/components/BackupSetList'
import BackupSetFiles from '@/components/BackupSetFiles'
import BackupSetFileVersions from '@/components/BackupSetFileVersions'
import Sources from '@/components/Sources'
import SourceList from '@/components/SourceList'
import SourceDetails from '@/components/SourceDetails'
import SourceEdit from '@/components/SourceEdit'
import SourceAddFolder from '@/components/SourceAddFolder'

Vue.use(Router);

export default new Router({
  routes: [
    {
      path: '/home',
      component: HomeView
    },
    {
      path: '/backupsets',
      component: BackupSets,
      children: [
        {path: '', component: BackupSetList},
        {path: ':id/files', name:'backupset-files', component: BackupSetFiles},
        {path: ':id/versions/:file_path', name:'backupset-fileversions', component: BackupSetFileVersions},
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
        { path: ':id/add-folder', name: 'source-path-select', component: SourceAddFolder },
      ]
    },
  ]
})
