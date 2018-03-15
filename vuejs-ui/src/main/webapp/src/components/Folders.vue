<template>
  <div class="container">

    <div>
      <table class="table">
        <thead>
        <tr>
          <th>Paths</th>
          <th>enabled</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="p in paths">
          <td>{{p}}</td>
          <td><img src="../assets/green.png" height="24"></td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>
  export default {
    name: 'Folders',
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
