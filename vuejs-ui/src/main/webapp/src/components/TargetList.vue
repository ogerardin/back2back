<template>
  <b-container>
      <b-table :items="targets" :fields="fields" hover>
        <template slot="id" slot-scope="data">
          <router-link v-bind:to="{name: 'target-details', params: {id: data.item.id}}">
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
          <b-button size="sm" variant="primary" :to="{name: 'target-edit', params: {id: data.item.id}}">
            Edit
          </b-button>
          <b-button size="sm" variant="danger" :to="{name: 'target-delete', params: {id: data.item.id}}">
            Delete
          </b-button>
        </template>
      </b-table>
  </b-container>
</template>

<script>
  export default {
    name: 'TargetList',
    data() {
      return {
        targets: [],
        fields: [
          'id',
          'enabled',
          // '_class',
          'description',
          'actions',
        ],
      };
    },
    mounted() {
      this.getTargets();
    },
    updated() {
      this.getTargets();
    },
    methods: {
      getTargets: function () {
        this.$http.get('http://localhost:8080/api/targets').then(response => {
          this.targets = response.data;
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
