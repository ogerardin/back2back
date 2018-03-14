<template>
  <div class="container">
    <div>
      <h2>Add new source folder</h2>
      <form v-on:submit="addFolder">
        <div class="form-group">
          <label for="add-name">Path</label>
          <input type="text" class="form-control" id="add-name" v-model="path"/>
          <ul>
            <li v-for="f in files">
              <button v-on:click.prevent="getFiles(f.path)">{{f.name}}</button>
            </li>
          </ul>
        </div>
        <button type="submit" class="btn btn-primary">Add</button>
        <router-link class="btn btn-default" v-bind:to="'/'">Cancel</router-link>
      </form>
    </div>
  </div>
</template>

<script>
  export default {
    name: 'SourceAddFolder',
    data() {
      return {
        path: null,
        files: [],
        source: {},
      }
    },
    mounted() {
      this.getSource(this.$route.params.id);
      this.getFiles('/')
    },
    methods: {
      getSource(id) {
        this.$http.get('http://localhost:8080/api/sources/' + id).then(response => {
          this.source = response.data;
        }, error => {
          console.log(error)
        });

      },
      getFiles(dir) {
        this.$http.get('http://localhost:8080/api/filesystem?dirOnly=true&dir=' + encodeURIComponent(dir)).then(response => {
          this.path = dir;
          this.files = response.data;
        }, error => {
          console.log(error)
        });
      },
      addFolder() {
        this.source.paths.push(this.path);
        this.$http.post('http://localhost:8080/api/sources', this.source).then(response => {
          this.source.id = response.data;
        }, error => {
          console.log(error)
        });
        this.$router.push('/sources');
      }
    }
  }
</script>

<!-- styling for the component -->
<style>
</style>
