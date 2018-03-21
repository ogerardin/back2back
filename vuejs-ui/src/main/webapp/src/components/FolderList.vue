<template>

  <b-container>
    <b-list-group
    <b-list-group-item v-for="p in paths" :key="p">
      &#x1f4c1; {{p}}
    </b-list-group-item>
    </b-list-view>
    {{totalFiles}} files / {{totalBytes}}
  </b-container>

</template>

<script>
  export default {
    name: 'FolderList',
    data() {
      return {
        paths: [],
        totalFiles: 0,
        totalBytes: '',
      };
    },
    mounted() {
      this.getSources();
    },
    updated() {
      this.getSources();
    },
    methods: {
      getSources: function () {
        let paths = [];
        let totalFiles = 0;
        let totalBytes = 0;
        this.$http.get('http://localhost:8080/api/sources').then(response => {
          let sources = response.data;
          sources.forEach(
            s => {
              if (s._class === '.FilesystemSource') {
                paths = paths.concat(s.paths);
                totalFiles += s.totalFiles;
                totalBytes += s.totalBytes;
              }
            }
          );
          this.paths = paths;
          this.totalFiles = totalFiles;
          this.totalBytes = humanReadableByteCount(totalBytes, false);
        }, error => {
          console.log(error)
        });

      }
    },
  }

  function humanReadableByteCount(bytes, si) {
    const unit = si ? 1000 : 1024;
    if (bytes < unit) return bytes + " bytes";
    const exp = Math.floor(Math.log(bytes) / Math.log(unit));
    const pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "B" : "iB");
    return (bytes / Math.pow(unit, exp)).toFixed(2) + " " + pre;
  }
</script>

<!-- styling for the component -->
<style>
</style>
