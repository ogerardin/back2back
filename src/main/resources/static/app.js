

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
