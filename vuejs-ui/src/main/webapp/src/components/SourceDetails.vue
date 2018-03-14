<template>
  <div class="container">

    <div>
      <h2>{{ source.id }}</h2>
      <b>Paths: </b>
      <div>{{ source.paths }}</div>
      <b>Enabled:</b>
      <div>{{ source.enabled }}</div>
      <br/>
      <router-link v-bind:to="'/sources'">Back to list</router-link>
    </div>
  </div>
</template>

<script>
  export default {
    name: 'SourceDetails',
    data() {
      return {source: {}};
    },
    created: function () {
      this.getSource(this.$route.params.id);
    },
    methods: {
      getSource(id) {
        this.$http.get('http://localhost:8080/api/sources/' + id).then(response => {
          this.source = response.data;
        }, error => {
          console.log(error)
        });

      }
    }
  }
</script>

<!-- styling for the component -->
<style>
</style>
