<template>
    <div>
      <table class="table">
        <thead>
        <tr>
          <th>Path</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="p in paths">
          <td>{{p}}</td>
        </tr>
        </tbody>
      </table>
    </div>
</template>

<script>
  export default {
    name: 'FolderList',
    data() {
      return {
        paths: []
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
        this.$http.get('http://localhost:8080/api/sources').then(response => {
          let sources = response.data;
          sources.forEach(
            s => paths = paths.concat(s.paths)
          );
          this.paths = paths;
        }, error => {
          console.log(error)
        });

      }
    },
  }
</script>

<!-- styling for the component -->
<style>
</style>
