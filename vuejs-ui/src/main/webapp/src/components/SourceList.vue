<template>
  <div>
    <!-- Modal Component -->
    <b-modal ref="editSourceModal" title="Edit Source" lazy="true" @ok="saveSource()">
      <source-edit ref="editSourceComponent" :source-id="selectedId">
        <div slot="title"></div> <!-- don't show title -->
        <div slot="buttons"></div> <!-- don't show buttons -->
      </source-edit>
    </b-modal>

    <b-table :items="sources" :fields="fields" hover>
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
        <div v-if="data.item.enabled"><img src="../assets/green.png" height="24"></div>
        <img v-else src="../assets/red.png" height="24">
      </template>
      <template slot="actions" slot-scope="data">
        <b-button size="sm" variant="primary" :to="{name: 'source-edit', params: {id: data.item.id}}">
          Edit
        </b-button>
        <b-button size="sm" variant="secondary" @click="editModal(data.item.id)">
          Edit (modal)
        </b-button>
<!--
        <b-button size="sm" variant="danger" :to="{name: 'source-delete', params: {id: data.item.id}}">
          Delete
        </b-button>
-->
      </template>
    </b-table>
  </div>
</template>

<script>
  import SourceEdit from "./SourceEdit";

  export default {
    components: {SourceEdit},
    name: 'SourceList',
    data() {
      return {
        sources: [],
        fields: [
          'id',
          'enabled',
          'name',
          // '_class',
          'description',
          'paths',
          'totalFiles',
          'totalBytes',
          'actions',
        ],
        selectedId: 0,
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
      },
      editModal(id) {
        this.selectedId = id;
        this.$refs.editSourceModal.show();
      },
      saveSource() {
        this.$refs.editSourceComponent.updateSource();
      },
    },
  }
</script>

<!-- styling for the component -->
<style>
</style>
