<template>
  <b-container fluid>

      <b-card title="Destinations">
        <backup-set-progress sourceClassFilter=".FilesystemSource"></backup-set-progress>
      </b-card>

      <b-card title="Source Folders">
        <folder-list></folder-list>
      </b-card>

      <b-card title="Inbound">
        <backup-set-progress sourceClassFilter=".PeerSource"></backup-set-progress>
      </b-card>
  </b-container>
</template>

<script>
  import BackupSetList from "./BackupSetList";
  import BackupSetProgress from "./BackupSetProgress";
  import FolderList from "./FolderList";

  export default {
    components: {
      BackupSetProgress,
      FolderList,
      BackupSetList,
    },
    name: 'HomeView',
    data() {
      return {}
    }
  }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
