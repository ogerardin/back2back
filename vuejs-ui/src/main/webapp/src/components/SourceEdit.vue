<template>
  <div class="container">
    <div>
      <h2>Edit source</h2>
      <form v-on:submit="updateSource">
        <div class="form-group" v-for="p in source.paths">
          Path: {{p}}
          <router-link class="btn btn-warning btn-xs" v-bind:to="{name: 'source-path-delete', params: {id: source.id, path: p}}">Remove</router-link>
        </div>
        <router-link class="btn btn-warning btn-xs" v-bind:to="{name: 'source-path-select', params: {id: source.id}}">Add folder</router-link>
        <div class="form-group">
          <label for="edit-price">Enabled</label>
          <input type="checkbox" class="form-control" id="edit-price" v-model="source.enabled"/>
        </div>
        <button type="submit" class="btn btn-primary">Save</button>
        <router-link class="btn btn-default" v-bind:to="'/sources'">Cancel</router-link>
      </form>
    </div>
  </div>
</template>

<script>
  export default {
    name: 'SourceEdit',
    data() {
      return {source: {}};
    },
    created() {
      this.getSource(this.$route.params.id);
    },
    methods: {
      getSource(id) {
        this.$http.get('http://localhost:8080/api/sources/' + id).then(response => {
          this.source = response.data;
        }, error => {
          console.log(error)
        });
      },
      updateSource() {
        const source = this.source;
        this.$http.post('http://localhost:8080/api/sources', source).then(response => {
          this.source.id = response.data;
        }, error => {
          console.log(error)
        });
        router.push('/sources');
      }
    }
  }
</script>

<!-- styling for the component -->
<style>
</style>
