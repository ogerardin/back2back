<template>
  <b-container>
    <slot name="title">
      <h2>Edit Target</h2>
    </slot>

    <b-form v-on:submit="updateTarget">

      <b-form-group label="ID:" label-for="id">
        <b-form-input id="id" v-model="target.id" readonly></b-form-input>
      </b-form-group>

      <b-form-group label="Name:" label-for="name">
        <b-form-input id="name" v-model="target.name"></b-form-input>
      </b-form-group>

      <template v-if="target._class=='.PeerTarget'">
        <b-form-group label="Hostname:" label-for="host">
          <b-form-input id="host" required v-model="target.hostname"></b-form-input>
        </b-form-group>
        <b-form-group label="Port:" label-for="port">
          <b-form-input id="port" required type="number" v-model="target.port"></b-form-input>
        </b-form-group>
      </template>

      <b-form-group>
        <b-form-checkbox v-model="target.enabled">Enabled</b-form-checkbox>
      </b-form-group>

      <slot name="buttons">
        <b-button variant="primary" type="submit">Save</b-button>
        <b-button variant="secondary" v-on:click="cancel()">Cancel</b-button>
      </slot>

    </b-form>
  </b-container>
</template>

<script>

  export default {
    name: 'TargetEdit',
    props: [
      'targetId',
    ],
    data() {
      return {
        target: {},
      };
    },
    mounted() {
      if (this.targetId) {
        this.getTarget(this.targetId);
      }
      else {
        this.getTarget(this.$route.params.id);
      }
    },
    methods: {
      getTarget(id) {
        this.$http.get('http://localhost:8080/api/targets/' + id).then(response => {
          this.target = response.data;
        }, error => {
          console.log(error)
        });
      },
      updateTarget() {
        const target = this.target;
        this.$http.put('http://localhost:8080/api/targets/' + target.id, target).then(response => {
          this.target = response.data;
          this.$router.go(-1);
        }, error => {
          console.log(error)
        });
      },
      cancel() {
        this.$router.go(-1);
      },
    }
  }
</script>

<!-- styling for the component -->
<style>
</style>
