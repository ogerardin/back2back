<template>
  <b-container>
    <h2>Files for backup set: {{ id }}</h2>
    <b-table small hover :items="files" :fields="fields">
      <template slot="deleted" slot-scope="data">
        {{data.item.deleted}}
      </template>
      <template slot="path" slot-scope="data">
        {{data.item.path}}
      </template>
      <template slot="actions" slot-scope="data">
        <b-button size="sm" variant="secondary" :to="{name: 'backupset-fileversions', params: {id: id, file_path: data.item.path}}">
          Versions
        </b-button>
      </template>
    </b-table>
  </b-container>
</template>

<script>
  export default {
    name: 'BackupSetFiles',
    data: function () {
      return {
        id: null,
        files: [],
        fields: [
          'deleted',
          'path',
          'actions',
        ]
      };
    },
    created: function () {
      console.log(this.$route);
      this.getFiles();
    },
    methods: {
      getFiles: function () {
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
