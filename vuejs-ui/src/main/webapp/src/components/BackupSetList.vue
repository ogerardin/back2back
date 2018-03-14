<template>
  <div class="container">
    <div>
      <table class="table">
        <thead>
        <tr>
          <th>ID</th>
          <th>Source</th>
          <th>Target</th>
          <th>Status</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="s in backupSets">
          <td>
            <router-link v-bind:to="{name: 'backupset-files', params: {id: s.id}}">{{s.id}}</router-link>
          </td>
          <td><router-link  v-bind:to="{name: 'source-details', params: {id: s.backupSource.id}}">
            {{s.backupSource.id}}</router-link><br>
            {{s.backupSource._class}}<br>
            {{s.backupSource.paths}}
          </td>
          <td>
            {{s.backupTarget.id}}<br>
            {{s.backupTarget._class}}
          </td>
          <td>{{s.status}}
            <div v-if="s.toDoCount!=0">
              {{s.toDoCount}} to do ({{s.toDoSize}} bytes)
            </div>
          </td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>
  export default {
    name: 'BackupSetList',
    data() {
      return {
        backupSets: []
      };
    },
    created() {
      this.getbackupSets();
    },
    methods: {
      getbackupSets() {
        this.$http.get('http://localhost:8080/api/backupsets').then(response => {
          this.backupSets = response.data;
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
