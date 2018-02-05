
const ListFiles = Vue.extend({
    template: '#file-list',
    data: function () {
        return {
            id: null,
            files: []
        };
    },
    created: function() {
        console.log(this.$route);
        this.getFiles();
    },
    methods: {
        getFiles: function() {
            id = this.$route.params.id;
            this.$http.get('/api/backupsets/' + id + '/files').then(response => {
                this.id = id;
                this.files = response.data;
            }, error => {
                // error callback
                console.log(error)
            });

        }
    }
});

const FileVersions = Vue.extend({
    template: '#versions-list',
    data: function () {
        return {
            id: null,
            versions: [],
            path: ''
        };
    },
    created: function() {
        console.log(this.$route);
        this.getFiles();
    },
    methods: {
        getFiles: function() {
            id = this.$route.params.id;
            path = this.$route.params.file_path;
            this.$http.get('/api/backupsets/' + id + '/versions?path=' + encodeURIComponent(path)).then(response => {
                this.id = id;
                this.path = path;
                this.versions = response.data;
            }, error => {
                // error callback
                console.log(error)
            });

        }
    }
});

let router = new VueRouter({
    routes: [
        {path: '/:id', component: ListFiles},
        {path: '/:id/:file_path', component: FileVersions, name:'file'},
    ],

});

app = new Vue({
    router: router,
}).$mount('#backupSet');