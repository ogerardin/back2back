<template>
  <div class="container">
    <div>
      <h2>File: {{ path }}</h2>
      <table>
        <thead>
        <tr>
          <th>Version ID</th>
          <th>Saved on</th>
          <th>Size</th>
          <th class="col-sm-2">Actions</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="v in versions">
          <td>{{ v.id }}</td>
          <td>{{ v.storedDate }}</td>
          <td>{{ v.size }}</td>
          <td>
            <a class="btn btn-default btn-xs"
               v-bind:href="'http://localhost:8080/api/backupsets/' + id + '/versions/' + v.id + '/contents'">Get
            </a>

          </td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>
  export default {
    name: 'BackupSetFileVersions',
    data() {
      return {
        id: null,
        versions: [],
        path: ''
      };
    },
    created() {
      console.log(this.$route);
      this.getFiles();
    },
    methods: {
      getFiles: function() {
        var id = this.$route.params.id;
        var path = this.$route.params.file_path;
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
