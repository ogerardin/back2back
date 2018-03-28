<template>
  <b-table :items="backupSets" :fields="fields">
    <template slot="index" slot-scope="data">
      {{data.index + 1}}
    </template>
    <template slot="id" slot-scope="data">
      <router-link v-bind:to="{name: 'backupset-files', params: {id: data.value}}">
        {{data.value}}
      </router-link>
    </template>
    <template slot="backupSource" slot-scope="data">
      <router-link v-bind:to="{name: 'source-edit', params: {id: data.value.id}}">
        {{data.value.description}}
      </router-link>
    </template>
    <template slot="backupTarget" slot-scope="data">
      <router-link v-bind:to="{name: 'target-edit', params: {id: data.value.id}}">
        {{data.value.description}}
      </router-link>
    </template>
    <template slot="status" slot-scope="data">
      {{data.value}}
      <div v-if="data.item.batchCount!=0">
        to do: {{data.item.toDoCount}} / {{data.item.batchCount}}
        <b-progress :max="data.item.batchCount" :value="data.item.batchCount - data.item.toDoCount"
                    variant="info" striped :animated="animate" class="mb-2"></b-progress>
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
      'sourceClassFilter', // optionnal value to filter by backupSet.sourceClass
    ],
    data() {
      return {
        backupSets: [],
        fields: [
          'index',
          // 'id',
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
