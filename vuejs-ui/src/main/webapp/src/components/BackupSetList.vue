<template>
  <b-table :items="backupSets" :fields="fields">
    <template slot="id" slot-scope="data">
      <router-link v-bind:to="{name: 'backupset-files', params: {id: data.value}}">
        {{data.value}}
      </router-link>
    </template>
    <template slot="backupSource" slot-scope="data">
      <router-link v-bind:to="{name: 'source-details', params: {id: data.value.id}}">
        {{data.value.id}}
      </router-link>
      <br/>
      {{data.value.description}}
    </template>
    <template slot="backupTarget" slot-scope="data">
      <router-link v-bind:to="{name: 'target-details', params: {id: data.value.id}}">
        {{data.value.id}}
      </router-link>
      <br/>
      {{data.value.description}}
    </template>
    <template slot="status" slot-scope="data">
      {{data.value}}
      <div v-if="data.item.toDoCount!=0">
        {{data.item.toDoCount}} to do ({{data.item.toDoSize}} bytes)
      </div>
    </template>
    <template slot="actions" slot-scope="data">
      <b-button size="sm" variant="secondary" :to="{name: 'backupset-files', params: {id: data.item.id}}">
        View files
      </b-button>
    </template>
  </b-table>
</template>

<script>
  export default {
    name: 'BackupSetList',
    props: [
      'sourceClassFilter',
    ],
    data() {
      return {
        backupSets: [],
        fields: [
          'id',
          'backupSource',
          'backupTarget',
          // 'description',
          'lastBackupCompleteTime',
          'currentBackupStartTime',
          'nextBackupTime',
          'fileCount',
          'size',
          // 'toDoCount',
          // 'toDoSize',
          // 'lastError',
          'status',
          'actions',
        ],
      };
    },
    created() {
      this.getbackupSets(this.sourceClassFilter);
    },
    methods: {
      getbackupSets(sourceClassFilter) {
        this.$http.get('http://localhost:8080/api/backupsets').then(response => {
          this.backupSets = response.data.filter(
            s => sourceClassFilter == null || s.backupSource._class === sourceClassFilter
          );
        }, error => {
          // error callback
          console.log(error)
        });

      }
    }
  }
</script>

<!-- styling for the component -->
<style>
</style>
