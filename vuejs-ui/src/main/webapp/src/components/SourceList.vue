<template>
  <b-container>

    <!-- Modal edit dialog -->
    <b-modal ref="editSourceModal" title="Edit Source" lazy
             v-model="showEditModal" @ok="saveSource()" @cancel="cancelEdit()">
      <!-- The modal dialog uses the SourceEdit component, with its sourceId prop mapped to this component's id prop -->
      <source-edit ref="editSourceComponent" :source-id="id">
        <div slot="title"></div> <!-- don't show title -->
        <div slot="buttons"></div> <!-- don't show buttons -->
      </source-edit>
    </b-modal>

    <b-table :items="sources" :fields="fields" hover>
      <template slot="index" slot-scope="data">
        {{data.index + 1}}
      </template>
      <template slot="id" slot-scope="data">
        <router-link v-bind:to="{name: 'source-details', params: {id: data.item.id}}">
          {{ data.item.id }}
        </router-link>
      </template>
      <template slot="paths" slot-scope="data">
        <template v-for="p in data.item.paths">
          {{p}}<br/>
        </template>
      </template>
      <template slot="enabled" slot-scope="data">
        <app-switch classes="is-warning" :checked="data.item.enabled" v-on:input="setEnabled(data.item, $event)"></app-switch>
      </template>
      <template slot="actions" slot-scope="data">
        <b-button size="sm" variant="primary" :to="{name: 'source-edit', params: {id: data.item.id}}">
          Edit
        </b-button>
        <b-button size="sm" variant="danger" v-on:click="deleteSource(data.item.id)">
          Delete
        </b-button>
      </template>
    </b-table>
  </b-container>
</template>

<script>
  import SourceEdit from "./SourceEdit";
  import AppSwitch from "./AppSwitch.vue";

  export default {
    name: 'SourceList',
    components: {
      SourceEdit,
      AppSwitch
    },
    props: [
      'id', /*This prop is */
    ],
    data() {
      return {
        sources: [],
        fields: [
          'index',
          //'id',
          'enabled',
          'name',
          // '_class',
          'description',
          //'paths',
          'totalFiles',
          'totalBytes',
          'actions',
        ],
        showEditModal: false,
      };
    },
    mounted() {
      this.getSources();
    },
    updated() {
      this.getSources();
    },
    watch: {
        '$route' (to, from) {
          // show the modal edit dialog if the matched route is 'source-edit'
          this.showEditModal = (this.$route.name === 'source-edit');
      }
    },
    methods: {
      getSources() {
        this.$http.get('http://localhost:8080/api/sources').then(response => {
          this.sources = response.data;
        }, error => {
          console.log(error)
        });
      },
      saveSource() {
        this.$refs.editSourceComponent.updateSource();
        this.$router.push({name: 'source-list'});
      },
      cancelEdit() {
        this.$router.push({name: 'source-list'});
      },
      deleteSource(id) {
        if (! confirm("Really delete Source? This action cannot be undone.")) {
          return;
        }
        this.$http.delete('http://localhost:8080/api/sources/' + id).then(response => {
          this.getSources();
        }, error => {
          console.log(error)
        });
      },
      setEnabled(source, enabled) {
        // console.log(target.id, target.enabled, enabled)
        if (source.enabled == enabled) {
          // no change: nothing to do
          return;
        }
        source.enabled = enabled;
        this.$http.put('http://localhost:8080/api/sources/' + source.id, source).then(response => {
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
