
const List = Vue.extend({
    template: '#product-list',
    data: function () {
        return {products: []};
    },
    created: function() {
        this.getSources();
    },
    methods: {
        getSources: function() {
            this.$http.get('/api/sources').then(response => {
                this.products = response.data;
            }, error => {
                // error callback
                console.log(error)
            });

        }
    }
});

const Product = Vue.extend({
    template: '#product',
    data: function () {
        return {product: {}};
    },
    created: function() {
        this.getSource(this.$route.params.source_id);
    },
    methods: {
        getSource: function(id) {
            this.$http.get('/api/sources/'+id).then(response => {
                this.product = response.data;
            }, error => {
                // error callback
                console.log(error)
            });

        }
    }
});

const ProductEdit = Vue.extend({
    template: '#product-edit',
    data: function () {
        return {product: {}};
    },
    created: function() {
        this.getSource(this.$route.params.source_id);
    },
    methods: {
        getSource: function(id) {
            this.$http.get('/api/sources/'+id).then(response => {
                this.product = response.data;
            }, error => {
                // error callback
                console.log(error)
            });

        },
        updateProduct: function () {
            const product = this.product;
            this.$http.post('/api/sources', product).then(response => {
                this.product.id = response.data;
            }, error => {
                // error callback
                console.log(error)
            });

            router.push('/');
        }
    }
});

const ProductDelete = Vue.extend({
    template: '#product-delete',
    data: function () {
        return {product: {}};
    },
    methods: {
        deleteProduct: function () {
            const product = this.product;
            this.$http.delete('/api/sources/' + this.$route.params.source_id).then(response => {
                //
            }, error => {
                // error callback
                console.log(error)
            });
            router.push('/');
        }
    }
});

const AddProduct = Vue.extend({
    template: '#add-product',
    data: function () {
        return {
            product: {
                '_class': '.FilesystemSource',
                enabled: false
        }}
    },
    methods: {
        createProduct: function () {
            const product = this.product;
            this.$http.post('/api/sources', JSON.stringify(product)).then(response => {
                this.product.id = response.data;
            }, error => {
                // error callback
                console.log(error)
            });
            router.push('/');
        }
    }
});

var router = new VueRouter({routes:[
  { path: '/', component: List},
  { path: '/product/:source_id', component: Product, name: 'product'},
  { path: '/add-product', component: AddProduct},
  { path: '/product/:source_id/edit', component: ProductEdit, name: 'product-edit'},
  { path: '/product/:source_id/delete', component: ProductDelete, name: 'product-delete'}
]});
app = new Vue({
  router:router
}).$mount('#app')