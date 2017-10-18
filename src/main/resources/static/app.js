
new Vue({
        el: '#sourcesElement',
        data: function() {
            return {
                sources: []
            };
        },
        created: function() {
            this.getSources();
        },
        methods: {
            getSources: function() {
                this.$http.get('/api/sources').then(response => {
                    this.sources = response.data;
                }, error => {
                    // error callback
                    console.log(error)
                });

            }
        }

    }
);

new Vue({
        el: '#targetsElement',
        data: function() {
            return {
                targets: []
            };
        },
        created: function() {
            this.getTargets();
        },
        methods: {
            getTargets: function() {
                this.$http.get('/api/targets').then(response => {
                    this.targets = response.data;
                }, error => {
                    // error callback
                    console.log(error)
                });

            }
        }
    }
);

new Vue({
        el: '#setsElement',
        data: function() {
            return {
                backupSets: []
            };
        },
        created: function() {
            this.getbackupSets();
        },
        methods: {
            getbackupSets: function() {
                this.$http.get('/api/backupsets').then(response => {
                    this.backupSets = response.data;
                }, error => {
                    // error callback
                    console.log(error)
                });

            }
        }
    }
);
