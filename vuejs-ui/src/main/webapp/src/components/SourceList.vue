<template>
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
          <router-link class="btn btn-warning btn-xs" v-bind:to="{name: 'source-edit', params: {id: data.item.id}}">
            Edit
          </router-link>
<!--
            <router-link class="btn btn-danger btn-xs" v-bind:to="{name: 'source-delete', params: {id: s.id}}">
              Delete
            </router-link>
-->
        </template>
      </b-table>

</template>

<script>
  export default {
    name: 'SourceList',
    data() {
      return {
        sources: [],
        fields: [
          'id',
          'enabled',
          '_class',
          // 'name',
          'paths',
          'totalFiles',
          'totalBytes',
          // 'description',
          'actions',
        ],
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
