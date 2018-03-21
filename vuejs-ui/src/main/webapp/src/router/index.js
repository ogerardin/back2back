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
import Targets from '@/components/Targets'
import TargetList from '@/components/TargetList'
import AdminPanel from '@/components/AdminPanel'

Vue.use(Router);

export default new Router({
  routes: [
    {
      path: '',
      redirect: '/home'
    },
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
        { path: '', name: 'source-list', component: SourceList },
        // { path: 'add', name: 'source-add', component: SourceAdd },
        { path: ':id', name: 'source-details', component: SourceDetails },
        { path: ':id/edit', name: 'source-edit', component: SourceEdit },
        // { path: ':id/edit', name: 'source-edit', component: SourceList, props: true },
        { path: ':id/add-folder', name: 'source-path-select', component: SourceAddFolder },
      ]
    },
    {
      path: '/targets',
      component: Targets,
      children: [
        { path: '', component: TargetList },
        // { path: 'add', name: 'source-add', component: SourceAdd },
        // { path: ':id', name: 'target-details', component: TargetDetails },
        // { path: ':id/edit', name: 'target-edit', component: TargetEdit },
        // { path: ':id/add-folder', name: 'target-path-select', component: TargetAddFolder },
      ]
    },
    {
      path: '/admin',
      component: AdminPanel,
    },
  ]
})
