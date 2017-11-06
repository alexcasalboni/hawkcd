package io.hawkcd.db.mongodb;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.util.JSON;
import io.hawkcd.db.IDbRepository;
import io.hawkcd.model.Entity;
import io.hawkcd.model.MaterialDefinition;
import io.hawkcd.model.TaskDefinition;
import io.hawkcd.utilities.deserializers.MaterialDefinitionAdapter;
import io.hawkcd.utilities.deserializers.TaskDefinitionAdapter;
import org.apache.log4j.Logger;
import org.bson.Document;

import javax.ws.rs.NotFoundException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.mongodb.client.model.Filters.eq;

public class MongoDbRepository<T extends Entity> implements IDbRepository<T> {
    private static final Logger LOGGER = Logger.getLogger(MongoDbRepository.class);
    private MongoCollection collection;
    private Type entryType;
    private Gson jsonConverter;
    private MongoDatabase mongoDatabase;

    public MongoDbRepository(Class<T> entry) {
        this.entryType = entry;
        this.jsonConverter = new GsonBuilder()
                .registerTypeAdapter(TaskDefinition.class, new TaskDefinitionAdapter())
                .registerTypeAdapter(MaterialDefinition.class, new MaterialDefinitionAdapter())
                .create();

        this.mongoDatabase = MongoDbManager.getInstance().getDb();
        this.collection = this.mongoDatabase.getCollection(this.entryType.getTypeName());
    }

    public MongoDbRepository(Class entry, MongoCollection mockedMongoCollection) {
        this.entryType = entry;
        this.jsonConverter = new GsonBuilder().create();
        this.collection = mockedMongoCollection;
    }

    @Override
    public T getById(String id) {
        if (id == null) {
            return null;
        }

        T result;
        try {
            //construct the filter
            UUID uuid = UUID.fromString(id);
            BasicDBObject bObj = new BasicDBObject("id", uuid);

            //execute the query against the db
            Document document = (Document) collection.find(eq("id", id)).first();
            if (document != null) {
                String json = document.toJson();
                result = this.jsonConverter.fromJson(json, this.entryType);
                return result;
            }

            return null;
        } catch (NotFoundException e) {
            LOGGER.error(e);
            throw e;
        }
    }

    @Override
    public List<T> getAll() {
        T resultElement;
        List<T> result = new ArrayList<>();
        try {
            FindIterable documents = this.collection.find();
            for (Object document : documents) {
                String documentToJson = JSON.serialize(document);
                resultElement = this.jsonConverter.fromJson(documentToJson, this.entryType);
                result.add(resultElement);
            }
        } catch (RuntimeException e) {
            LOGGER.error(e);
        }

        return result;
    }

    @Override
    public T add(T entry) {
        if (entry == null) {
            return null;
        }

        if (this.getById(entry.getId()) == null) {
            try {
                String entryToJson = this.jsonConverter.toJson(entry);
                Document document = Document.parse(entryToJson);
                this.collection.insertOne(document);
                return entry;
            } catch (RuntimeException e) {
                LOGGER.error(e);
            }
        } else {
            return null;
        }

        return null;
    }

    @Override
    public T update(T entry) {
        if (entry == null) {
            return null;
        }

        try {
            String entryToJson = this.jsonConverter.toJson(entry);
            Document document = Document.parse(entryToJson);

            UpdateResult updateResult = this.collection.replaceOne(eq("id", document.get("id")), document);

            if (updateResult.getMatchedCount() == 1) { // means one record updated
                return entry;
            }

            return null; //either none or many records updated, so consider the operation not successful.
        } catch (RuntimeException e) {
            LOGGER.error(e);
            return null;
        }
    }

    @Override
    public T delete(String id) {
        if (id == null) {
            return null;
        }

        T result;
        try {
            //construct the filter
            UUID uuid = UUID.fromString(id);
            BasicDBObject query = new BasicDBObject("id", uuid);

            Document document = (Document) this.collection.findOneAndDelete(eq("id", id));
            if (document != null) {
                String dd = document.toJson();
                result = this.jsonConverter.fromJson(dd, this.entryType);
                return result;
            } else {
                return null;
            }
        } catch (RuntimeException e) {
            LOGGER.error(e);
            return null;
        }
    }

    public List<T> QueryExecutor(BasicDBObject query){
        return QueryUnifier(query);
    }

    public List<T> QueryExecutor(BasicDBObject query, BasicDBObject sortingFilter){
        return QueryUnifier(query, sortingFilter);
    }

    public List<T> QueryExecutor(BasicDBObject query, BasicDBObject sortingFilter, Integer skip){
        return QueryUnifier(query, sortingFilter, skip);
    }

    public List<T> QueryExecutor(BasicDBObject query, BasicDBObject sortingFilter, Integer skip, Integer limit){
        return QueryUnifier(query, sortingFilter, skip, limit);
    }

    private List<T> QueryUnifier(Object... queryParams){
        T resultElement;
        List<T> result = new ArrayList<>();

        try {
            FindIterable documents = null;
            switch (queryParams.length) {
                case 0:
                    return result;
                case 1:
                    documents = collection.find((BasicDBObject)queryParams[0]);
                    break;
                case 2:
                    documents = collection.find((BasicDBObject)queryParams[0]).sort((BasicDBObject)queryParams[1]);
                    break;
                case 3:
                    documents = collection.find((BasicDBObject)queryParams[0]).sort((BasicDBObject)queryParams[1]).skip((Integer)queryParams[2]);
                    break;
                case 4:
                    documents = collection.find((BasicDBObject)queryParams[0]).sort((BasicDBObject)queryParams[1]).skip((Integer)queryParams[2]).limit((Integer)queryParams[3]);
                    break;
            }

            for (Object document : documents) {
                String documentToJson = JSON.serialize(document);
                resultElement = this.jsonConverter.fromJson(documentToJson, this.entryType);
                result.add(resultElement);
            }
        } catch (RuntimeException e) {
            LOGGER.error(e);
        }

        return result;
    }
}