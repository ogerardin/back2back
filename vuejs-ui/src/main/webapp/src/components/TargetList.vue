<template>
  <b-container>
      <b-table :items="targets" :fields="fields" hover>
        <template slot="index" slot-scope="data">
          {{data.index + 1}}
        </template>
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
<!--
        <template slot="enabled" slot-scope="data">
          <div v-if="data.item.enabled"><img src="../assets/green.png" height="24"></div>
          <img v-else src="../assets/red.png" height="24">
        </template>
-->
        <template slot="enabled" slot-scope="x">
          <app-switch classes="is-warning" :checked="x.item.enabled"></app-switch>
        </template>
        <template slot="actions" slot-scope="data">
          <b-button size="sm" variant="primary" :to="{name: 'target-edit', params: {id: data.item.id}}">
            Edit
          </b-button>
          <b-button size="sm" variant="danger" v-on:click="deleteTarget(data.item.id)">
            Delete
          </b-button>
        </template>
      </b-table>

    <b-button variant="secondary" :to="{name: 'target-add'}">
      Add Target
    </b-button>
  </b-container>
</template>

<script>

  import AppSwitch from './AppSwitch.vue'

  export default {
    name: 'TargetList',
    components: {
      AppSwitch
    },
    data() {
      return {
        targets: [],
        fields: [
          'index',
          //'id',
          'enabled',
          'name',
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
      getTargets() {
        this.$http.get('http://localhost:8080/api/targets').then(response => {
          this.targets = response.data;
        }, error => {
          console.log(error)
        });
      },
      deleteTarget(id) {
        if (! confirm("Really delete Target? This action cannot be undone.")) {
          return;
        }
        this.$http.delete('http://localhost:8080/api/targets/' + id).then(response => {
          this.getTargets();
        }, error => {
          console.log(error)
        });
      },

    },
  }
</script>

<!-- styling for the component -->
<style>
</style>
