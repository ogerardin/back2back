<template>

  <b-container>
    <b-card v-for="b in backupSets" :key="b.id">
      <b>{{b.backupSource.description}} &rarr; {{b.backupTarget.description}}</b><br>
      {{b.status}}
      <div v-if="b.batchCount!=0">
        to do: {{b.toDoCount}} / {{b.batchCount}}
        <b-progress :max="b.batchCount" :value="b.batchCount - b.toDoCount"
                    variant="info" striped :animated="animate" class="mb-2"></b-progress>
      </div>
      <div v-else>
        <b-progress :max="b.fileCount" :value="b.fileCount"
                    variant="info" class="mb-2"></b-progress>
      </div>
    </b-card>
  </b-container>

</template>

<script>
  export default {
    name: 'BackupSetProgress',
    props: [
      'sourceClassFilter', // optionnal value to filter by backupSet.sourceClass
    ],
    data() {
      return {
        backupSets: [],
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
