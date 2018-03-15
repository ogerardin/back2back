<template>
  <div>
    <div>
      <h2>Backup Jobs</h2>
      <backup-set-list sourceClassFilter=".FilesystemSource"></backup-set-list>
    </div>

    <div>
      <folders></folders>
    </div>

    <div>
      <h2>Incoming</h2>
      <backup-set-list sourceClassFilter=".PeerSource"></backup-set-list>
    </div>
  </div>
</template>

<script>
  import BackupSetList from "./BackupSetList";
  import Folders from "./Folders";

  export default {
    components: {
      Folders,
      BackupSetList},
    name: 'Home',
    data() {
      return {}
    }
  }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
