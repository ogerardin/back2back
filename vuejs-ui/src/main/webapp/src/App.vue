<template>
  <div id="app">
    <b-navbar toggleable type="dark" variant="info">

      <b-navbar-brand> <img src="./assets/logo.png"></b-navbar-brand>

      <b-navbar-nav>
        <b-nav-item v-bind:to="'/home'">Home</b-nav-item>
        <b-nav-item v-bind:to="'/sources'">Sources</b-nav-item>
        <b-nav-item v-bind:to="'/targets'">Targets</b-nav-item>
        <b-nav-item v-bind:to="'/backupsets'">Backup Sets</b-nav-item>
        <b-nav-item v-bind:to="'/admin'">Admin</b-nav-item>
      </b-navbar-nav>
    </b-navbar>

      <router-view/>
  </div>
</template>

<script>
export default {
  name: 'App'
}
</script>

<style>
#app {
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  /*text-align: center;*/
  /*color: #2c3e50;*/
  /*margin-top: 60px;*/
}
</style>
