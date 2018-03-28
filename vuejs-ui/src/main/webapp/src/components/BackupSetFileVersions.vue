<template>
    <b-container>
      <h2>File: {{ path }}</h2>
      <b-table small hover :items="versions" :fields="fields">
        <template slot="actions" slot-scope="data">
          <b-button size="sm" variant="secondary" :href="'http://localhost:8080/api/backupsets/' + id + '/versions/' + data.item.id + '/contents'">
            Get
          </b-button>
        </template>
      </b-table>
    </b-container>
</template>

<script>
  export default {
    name: 'BackupSetFileVersions',
    data() {
      return {
        id: null,
        versions: [],
        path: '',
        fields: [
          'id',
          // 'filename',
          'storedDate',
          'size',
          'md5hash',
          'actions'
        ]
      };
    },
    created() {
      console.log(this.$route);
      this.getFiles();
    },
    methods: {
      getFiles: function() {
        let id = this.$route.params.id;
        let path = this.$route.params.file_path;
        this.$http.get('http://localhost:8080/api/backupsets/' + id + '/versions?path=' + encodeURIComponent(path)).then(response => {
          this.id = id;
          this.path = path;
          this.versions = response.data;
        }, error => {
          // error callback
          console.log(error)
        });

      }
    }
  }
</script>

<!-- styling for the component -->
<style>
</style>
