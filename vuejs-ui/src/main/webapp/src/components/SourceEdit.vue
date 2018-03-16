<template>
    <div>
      <h2>Edit source</h2>

      <b-form v-on:submit="updateSource">
        <b-form-group
          label="ID"
          label-for="id">
          <b-form-input id="id" v-model="source.id" readonly="true"></b-form-input>
        </b-form-group>
        <b-form-group
          label="Name"
          label-for="name">
          <b-form-input id="name" v-model="source.name"></b-form-input>
        </b-form-group>

        <b-form-group label="Folders">
        <b-input-group prepend="Folder" v-for="p in source.paths">
          <b-form-input readonly :value="p"></b-form-input>
          <b-input-group-append>
            <b-btn variant="warning" v-on:click="removeFolder(p)">Remove</b-btn>
          </b-input-group-append>
        </b-input-group>
        <b-button size="sm" variant="secondary" :to="{name: 'source-path-select', params: {id: source.id}}">
          Add Folder
        </b-button>
        </b-form-group>

        <b-form-group>
        <b-form-checkbox v-model="source.enabled">Enabled</b-form-checkbox>
        </b-form-group>
        <b-button type="submit" variant="primary">
          Save
        </b-button>
        <b-button variant="secondary" :to="'/sources'">
          Cancel
        </b-button>
      </b-form>
    </div>
</template>

<script>

  export default {
    name: 'SourceEdit',
    data() {
      return {
        source: {},
      };
    },
    mounted() {
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
        this.$router.push('/sources');
      },
      removeFolder(path) {
        let index = this.source.paths.indexOf(path);
        if (index >= 0) {
          this.source.paths.splice(index, 1)
        }
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
