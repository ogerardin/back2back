<template>
  <div class="container">

    <div>
      <table class="table">
        <thead>
        <tr>
          <th>ID</th>
          <th>Paths</th>
          <th>enabled</th>
          <th class="col-sm-2">Actions</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="s in sources">
          <td>
            <router-link v-bind:to="{name: 'source-details', params: {id: s.id}}">
              {{ s.id }}
            </router-link>
          </td>
          <td>{{ s.paths }}</td>
          <td>{{ s.enabled }}
            <div v-if="s.enabled"><img src="../assets/green.png" height="24"></div>
            <img v-else src="../assets/red.png" height="24">
          </td>

          <td>
            <router-link class="btn btn-warning btn-xs" v-bind:to="{name: 'source-edit', params: {id: s.id}}">
              Edit
            </router-link>
<!--
            <router-link class="btn btn-danger btn-xs" v-bind:to="{name: 'source-delete', params: {id: s.id}}">
              Delete
            </router-link>
-->
          </td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>
  export default {
    name: 'SourceList',
    data() {
      return {
        sources: []
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
        this.$http.get('http://localhost:8080/api/sources').then(response => {
          this.sources = response.data;
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
