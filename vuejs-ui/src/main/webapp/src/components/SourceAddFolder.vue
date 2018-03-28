<template>
    <b-container>
      <slot name="title">
        <h2>Add new source folder</h2>
      </slot>

      <b-form v-on:submit="addFolder">
        <b-form-group label="Selected Folder" label-for="path">
          <b-form-input id="path" v-model="path" readonly></b-form-input>
        </b-form-group>

        <b-form-group label="Roots">
          <b-button v-for="f in roots" :key="f.name"
                    variant="link" v-on:click.prevent="getFolders(f.path)"> &#x1f4c1; {{f.name}}</b-button>
        </b-form-group>

        <b-form-group label="Sub-folders">
            <b-button v-for="f in files" :key="f.name"
                        variant="link" v-on:click.prevent="getFolders(f.path)"> &#x1f4c1; {{f.name}}</b-button>
        </b-form-group>

        <slot name="buttons">
          <b-button variant="primary" type="submit">Add</b-button>
          <b-button variant="secondary" v-on:click="cancel()">Cancel</b-button>
        </slot>
      </b-form>
    </b-container>
</template>

<script>
  export default {
    name: 'SourceAddFolder',
    data() {
      return {
        roots: [],
        path: null,
        files: [],
        source: {},
      }
    },
    mounted() {
      this.getSource(this.$route.params.id);
      this.getRoots()
    },
    methods: {
      getSource(id) {
        this.$http.get('http://localhost:8080/api/sources/' + id).then(response => {
          this.source = response.data;
        }, error => {
          console.log(error)
        });

      },
      getRoots() {
        this.$http.get('http://localhost:8080/api/filesystem/roots').then(response => {
          this.roots = response.data;
          // by default we start in the first root folder
          this.getFolders(this.roots[0].path)
        }, error => {
          console.log(error)
        });
      },
      getFolders(dir) {
        this.$http.get('http://localhost:8080/api/filesystem?dirOnly=true&dir=' + encodeURIComponent(dir)).then(response => {
          this.path = dir;
          this.files = response.data;
        }, error => {
          console.log(error)
        });
      },
      addFolder() {
        this.source.paths.push(this.path);
        this.$http.put('http://localhost:8080/api/sources/' + this.source.id, this.source).then(response => {
          this.source = response.data;
          this.$router.go(-1)
        }, error => {
          console.log(error)
        });
      },
      cancel() {
        this.$router.go(-1);
      },
    }
  }
</script>

<!-- styling for the component -->
<style>
</style>
