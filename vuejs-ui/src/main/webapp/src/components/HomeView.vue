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
  import BackupSetProgress from "@/components/BackupSetProgress";
  import FolderList from "@/components/FolderList";

  export default {
    components: {
      BackupSetProgress,
      FolderList,
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
