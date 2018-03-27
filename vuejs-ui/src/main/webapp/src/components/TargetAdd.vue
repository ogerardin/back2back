<template>
  <b-container>
    <slot name="title">
      <h2>Add Target</h2>
    </slot>

    <b-form @submit="addTarget">

      <b-form-group label="Type:" label-for="type">
        <b-form-select id="type" required v-model="target._class" :options="options" />
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
        <p>Peer URL: http://{{target.hostname}}:{{target.port}}</p>
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
    name: 'TargetAdd',
    props: [
    ],
    data() {
      return {
        target: {
          _class: null,
          port:8080,
          enabled: true,
        },
        options: [
          { value: null, text: 'Please select an option' },
          { value: '.LocalTarget', text: 'Internal storage' },
          { value: '.PeerTarget', text: 'Peer back2back' },
          { value: '.LocalFolder', text: 'Local directory', disabled: true }
        ]
      };
    },
    mounted() {
    },
    methods: {
      addTarget(evt) {
        evt.preventDefault();
        const target = this.target;
        this.$http.post('http://localhost:8080/api/targets', target).then(response => {
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
