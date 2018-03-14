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
        <tr v-for="source in sources">
          <td>
            <router-link v-bind:to="{name: 'source-details', params: {id: source.id}}">{{ source.id }}
            </router-link>
          </td>
          <td>{{ source.paths }}</td>
          <td>{{ source.enabled }}</td>
          <td>
            <router-link class="btn btn-warning btn-xs"
                         v-bind:to="{name: 'source-edit', params: {id: source.id}}">Edit
            </router-link>
            <router-link class="btn btn-danger btn-xs"
                         v-bind:to="{name: 'source-delete', params: {id: source.id}}">Delete
            </router-link>
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
      return {sources: []};
    },
    created() {
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
    }
  }
</script>

<!-- styling for the component -->
<style>
</style>
