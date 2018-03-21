<template>

  <b-container>
    <b-card v-for="p in paths">
      {{p}}
    </b-card>
  </b-container>

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
            s => {
              if (s._class === '.FilesystemSource') {
                paths = paths.concat(s.paths)
              }
            }
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
