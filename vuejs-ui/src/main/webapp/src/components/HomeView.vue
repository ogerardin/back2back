<template>
  <div>
    <div>
      <h2>Backup Jobs</h2>
      <backup-set-list sourceClassFilter=".FilesystemSource"></backup-set-list>
    </div>

    <div>
      <h2>Source Folders</h2>
      <folder-list></folder-list>
    </div>

    <div>
      <h2>Incoming</h2>
      <backup-set-list sourceClassFilter=".PeerSource"></backup-set-list>
    </div>
  </div>
</template>

<script>
  import BackupSetList from "./BackupSetList";
  import FolderList from "./FolderList";

  export default {
    components: {
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
