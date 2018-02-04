const ListSources = Vue.extend({
    template: '#source-list',
    data: function () {
        return {sources: []};
    },
    created: function () {
        this.getSources();
    },
    methods: {
        getSources: function () {
            this.$http.get('/api/sources').then(response => {
                this.sources = response.data;
            }, error => {
                console.log(error)
            });

        }
    }
});

const Source = Vue.extend({
    template: '#source',
    data: function () {
        return {source: {}};
    },
    created: function () {
        this.getSource(this.$route.params.source_id);
    },
    methods: {
        getSource: function (id) {
            this.$http.get('/api/sources/' + id).then(response => {
                this.source = response.data;
            }, error => {
                console.log(error)
            });

        }
    }
});

const SourceEdit = Vue.extend({
    template: '#source-edit',
    data: function () {
        return {source: {}};
    },
    created: function () {
        this.getSource(this.$route.params.source_id);
    },
    methods: {
        getSource: function (id) {
            this.$http.get('/api/sources/' + id).then(response => {
                this.source = response.data;
            }, error => {
                console.log(error)
            });
        },
        updateSource: function () {
            const source = this.source;
            this.$http.post('/api/sources', source).then(response => {
                this.source.id = response.data;
            }, error => {
                console.log(error)
            });

            router.push('/');
        }
    }
});

const SourceDelete = Vue.extend({
    template: '#source-delete',
    data: function () {
        return {source: {}};
    },
    methods: {
        deleteSource: function () {
            const source = this.source;
            this.$http.delete('/api/sources/' + this.$route.params.source_id).then(response => {
                //TODO
            }, error => {
                console.log(error)
            });
            router.push('/');
        }
    }
});

const AddSource = Vue.extend({
    template: '#add-source',
    data: function () {
        return {
            source: {
                '_class': '.FilesystemSource',
                enabled: false,
                path: '/'
            },
            files: []
        }
    },
    created: function () {
        this.getFiles(this.source.path)
    },
    methods: {
        createSource: function () {
            const source = this.source;
            source._class = '.FilesystemSource';
            this.$http.post('/api/sources', source).then(response => {
                this.source.id = response.data;
            }, error => {
                console.log(error)
            });
            router.push('/');
        },
        getFiles: function (dir) {
            this.$http.get('/api/filesystem?dirOnly=true&dir=' + encodeURIComponent(dir)).then(response => {
                this.source.path = dir;
                this.files = response.data;
            }, error => {
                console.log(error)
            });
        }
    }
});

let router = new VueRouter({
    routes: [
        {path: '/', component: ListSources},
        {path: '/add-source', component: AddSource},
        {path: '/source/:source_id', component: Source, name: 'source'},
        {path: '/source/:source_id/edit', component: SourceEdit, name: 'source-edit'},
        {path: '/source/:source_id/delete', component: SourceDelete, name: 'source-delete'}
    ]
});

app = new Vue({
    router: router
}).$mount('#app');