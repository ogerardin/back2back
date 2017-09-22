package org.ogerardin.b2b.repo;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.gridfs.GridFS;
import org.ogerardin.b2b.domain.FileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import java.io.*;
import java.util.Arrays;
import java.util.Random;

@EnableAutoConfiguration
@ComponentScan(basePackages = {"org.ogerardin.b2b.mongo"})
public class GridFsTest implements CommandLineRunner {

    @Autowired
    private MongoDbFactory dbFactory;

    @Autowired
    private MongoConverter converter;

//    @Autowired
//    GridFsTemplate gridFsTemplate;

    public static void main(String[] args) {
        new SpringApplicationBuilder(GridFsTest.class)
                .web(false)
                .run(args);
    }


    @Override
    public void run(String... args) throws Exception {

        Random rands = new Random();

        GridFsTemplate gridFsTemplate = new GridFsTemplate(dbFactory, converter, ""+rands.nextLong());

        //gridFsTemplate.delete(new Query());

        for (File file : new File("/Users/olivier/Downloads").listFiles((dir, name) -> name.endsWith(".pdf"))) {
            if (file.isFile()) {
                InputStream is = null;
                is = new BufferedInputStream(new FileInputStream(file));
                gridFsTemplate.store(is, file.getCanonicalPath());
            }
        }

        gridFsTemplate.find(new Query())
                .forEach(f -> System.out.println(f.getFilename()));

        Thread.sleep(100000);


    }
}
