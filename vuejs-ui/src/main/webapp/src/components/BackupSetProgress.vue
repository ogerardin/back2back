<template>

  <b-container>
    <b-card v-for="b in backupSets" :key="b.id">
      <b>{{b.backupSource.description}} &rarr; {{b.backupTarget.description}}</b><br>
      {{b.status}}
      <div v-if="b.batchCount!=0">
        to do: {{b.toDoCount}} / {{b.batchCount}}
        <b-progress :max="b.batchCount" :value="b.batchCount - b.toDoCount"
                    variant="info" striped animated class="mb-2"></b-progress>
      </div>
      <div v-else>
        <b-progress :max="b.fileCount" :value="b.fileCount"
                    variant="info" class="mb-2"></b-progress>
      </div>
    </b-card>
  </b-container>

</template>

<script>
  import SockJS from 'sockjs-client'
  import Stomp from "webstomp-client"

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
    mounted() {
      this.connect();
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

      },
/*
      send() {
        if (this.stompClient && this.stompClient.connected) {
          console.log("Send message");
          const msg = {
            message: "TEST",
          };
          this.stompClient.send("/hello", JSON.stringify(msg), {});
        }
      },
*/
      connect() {
        if (this.stompClient && this.stompClient.connected) {
          return;
        }
        this.socket = new SockJS("http://localhost:8080/websocket");
        this.stompClient = Stomp.over(this.socket, { debug: false, heartbeat: false, protocols: ['v12.stomp'] } );
        this.stompClient.connect(
          {},
          connectFrame => {
            console.log(connectFrame);
            this.stompClient.subscribe("/topic/message", messageFrame => {
              // console.log("Received message on topic /topic/message");
              console.log(messageFrame);
              // this.received_messages.push(JSON.parse(messageFrame.body).content);
              var bsu = JSON.parse(messageFrame.body);
              // console.log(bsu.id);

              for (const bs of this.backupSets) {
                if (bs.id === bsu.id) {
                  console.log("updating " + bs.id);
                  bs.status = bsu.status;
                  bs.size = bsu.size;
                  bs.batchCount = bsu.batchCount;
                  bs.batchSize = bsu.batchSize;
                  bs.toDoCount = bsu.toDoCount;
                  bs.toDoSize = bsu.toDoSize;
                }
              }
            });
          },
          error => {
            console.log(error);
          }
        );
      },
      disconnect() {
        if (this.stompClient) {
          this.stompClient.disconnect();
        }
      },
      /*
            tickleConnection() {
              this.connected ? this.disconnect() : this.connect();
            },
      */
    }
  }
</script>

<!-- styling for the component -->
<style>
</style>
