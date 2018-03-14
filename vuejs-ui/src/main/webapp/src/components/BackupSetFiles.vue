<template>
  <div class="container">
    <div>
      <h2>Backup set: {{ id }}</h2>
      <table>
        <thead>
        <tr>
          <th>File path</th>
          <th class="col-sm-2">Actions</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="f in files">
          <td>{{ f }}</td>
          <td>
            <router-link class="btn btn-default btn-xs" v-bind:to="{name: 'backupset-fileversions', params: {file_path: f}}">Versions</router-link>
          </td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>
  export default {
    name: 'BackupSetFiles',
    data: function () {
      return {
        id: null,
        files: []
      };
    },
    created: function() {
      console.log(this.$route);
      this.getFiles();
    },
    methods: {
      getFiles: function() {
        var id = this.$route.params.id;
        this.$http.get('http://localhost:8080/api/backupsets/' + id + '/files').then(response => {
          this.id = id;
          this.files = response.data;
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
