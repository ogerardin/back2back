<template>
  <b-container>
    <slot name="title">
      <h2>Edit source</h2>
    </slot>

    <b-form v-on:submit="updateSource">
      <b-form-group label="ID" label-for="id">
        <b-form-input id="id" v-model="source.id" readonly></b-form-input>
      </b-form-group>

      <b-form-group label="Name" label-for="name">
        <b-form-input id="name" v-model="source.name"></b-form-input>
      </b-form-group>

      <template v-if="source._class=='.FilesystemSource'">
        <b-form-group label="Folders">
          <b-input-group prepend="Folder" v-for="p in source.paths" :key="p">
            <b-form-input readonly :value="p"></b-form-input>
            <b-input-group-append>
              <b-btn variant="warning" v-on:click="removeFolder(p)">Remove</b-btn>
            </b-input-group-append>
          </b-input-group>

          <b-button size="sm" variant="secondary" :to="{name: 'source-path-select', params: {id: source.id}}">
            Add Folder
          </b-button>
        </b-form-group>
      </template>

      <b-form-group>
        <b-form-checkbox v-model="source.enabled">Enabled</b-form-checkbox>
      </b-form-group>

      <slot name="buttons">
        <b-button variant="primary" type="submit">Save</b-button>
        <b-button variant="secondary" v-on:click="cancel()">Cancel</b-button>
      </slot>
    </b-form>
  </b-container>
</template>

<script>

  export default {
    name: 'SourceEdit',
    props: [
      'sourceId',
    ],
    data() {
      return {
        source: {},
      };
    },
    mounted() {
      if (this.sourceId) {
        this.getSource(this.sourceId);
      }
      else {
        this.getSource(this.$route.params.id);
      }
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
        this.$http.put('http://localhost:8080/api/sources/' + source.id, source).then(response => {
          this.source = response.data;
          this.$router.go(-1);
        }, error => {
          console.log(error)
        });
      },
      removeFolder(path) {
        let index = this.source.paths.indexOf(path);
        if (index >= 0) {
          this.source.paths.splice(index, 1)
        }
        this.updateSource();
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
