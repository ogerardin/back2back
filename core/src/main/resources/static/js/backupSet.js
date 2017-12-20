
const ListFiles = Vue.extend({
    template: '#file-list',
    data: function () {
        return {files: []};
    },
    created: function() {
        console.log(this.$route);
        this.getFiles();
    },
    methods: {
        getFiles: function() {
            id = this.$route.query.id;
            this.$http.get('/api/backupsets/' + id + '/files').then(response => {
                this.files = response.data;
            }, error => {
                // error callback
                console.log(error)
            });

        }
    }
});

let router = new VueRouter({
    routes: [
        {path: '/', component: ListFiles},
    ],

});

app = new Vue({
    router: router,
}).$mount('#backupSet');