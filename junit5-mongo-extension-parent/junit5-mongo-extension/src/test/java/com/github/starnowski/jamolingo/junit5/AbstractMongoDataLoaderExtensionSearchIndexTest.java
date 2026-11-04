package com.github.starnowski.jamolingo.junit5;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mongodb.MongoCommandException;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import java.util.Optional;
import java.util.function.Supplier;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonString;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

class AbstractMongoDataLoaderExtensionSearchIndexTest {

  private MongoClient mongoClient;
  private MongoDatabase firstDatabase;
  private MongoDatabase secondDatabase;
  private MongoCollection<Document> firstDbFirstCol;
  private MongoCollection<Document> firstDbSecondCol;
  private MongoCollection<Document> secondDbFirstCol;
  private AbstractMongoDataLoaderExtension tested;

  @BeforeEach
  @SuppressWarnings("unchecked")
  void setUp() {
    mongoClient = mock(MongoClient.class);
    firstDatabase = mock(MongoDatabase.class);
    secondDatabase = mock(MongoDatabase.class);
    firstDbFirstCol = mock(MongoCollection.class);
    firstDbSecondCol = mock(MongoCollection.class);
    secondDbFirstCol = mock(MongoCollection.class);

    when(mongoClient.getDatabase("db1")).thenReturn(firstDatabase);
    when(mongoClient.getDatabase("db2")).thenReturn(secondDatabase);
    when(firstDatabase.getCollection("col1")).thenReturn(firstDbFirstCol);
    when(firstDatabase.getCollection("col2")).thenReturn(firstDbSecondCol);
    when(secondDatabase.getCollection("col1")).thenReturn(secondDbFirstCol);

    tested =
        new AbstractMongoDataLoaderExtension() {
          @Override
          protected MongoClient resolveMongoClient() {
            return mongoClient;
          }
        };
  }

  @MongoSetup(indexes = {@SearchIndex(database = "db1", collection = "col1")})
  private void setupDropOnly() {}

  @MongoSetup(
      indexes = {@SearchIndex(database = "db1", collection = "col1", fieldName = "plainString")})
  private void setupFieldNameDefaultIndexValue() {}

  @MongoSetup(
      indexes = {
        @SearchIndex(
            database = "db1",
            collection = "col1",
            fieldName = "createdAt",
            indexValue = -1,
            indexOptionsSupplier = UniqueNamedIndexOptionsSupplier.class)
      })
  private void setupFieldNameCustomIndexValueAndOptions() {}

  @MongoSetup(
      indexes = {
        @SearchIndex(
            database = "db1",
            collection = "col1",
            bsonSupplier = CompoundBsonSupplier.class,
            indexOptionsSupplier = UniqueNamedIndexOptionsSupplier.class)
      })
  private void setupCustomBsonSupplierAndOptionsSupplier() {}

  @MongoSetup(
      indexes = {
        @SearchIndex(
            database = "db1",
            collection = "col1",
            fieldName = "priorityField",
            indexValue = -1,
            bsonSupplier = CompoundBsonSupplier.class)
      })
  private void setupBothFieldNameAndBsonSupplier() {}

  @MongoSetup(
      indexes = {
        @SearchIndex(database = "db1", collection = "col1", fieldName = "fieldA"),
        @SearchIndex(
            database = "db1",
            collection = "col2",
            bsonSupplier = CompoundBsonSupplier.class),
        @SearchIndex(database = "db1", collection = "col1", fieldName = "fieldB", indexValue = -1),
        @SearchIndex(database = "db2", collection = "col1"),
        @SearchIndex(database = "db1", collection = "col2", fieldName = "fieldC")
      })
  private void setupGroupedMultipleCollectionsAndDatabases() {}

  @Test
  void shouldOnlyDropIndexesAndSkipCreateIndexWhenNeitherFieldNameNorBsonSupplierProvided()
      throws Exception {
    // GIVEN
    ExtensionContext extensionContext = mockExtensionContext("setupDropOnly");

    // WHEN
    tested.beforeEach(extensionContext);

    // THEN
    verify(firstDbFirstCol, times(1)).dropIndexes();
    verify(firstDbFirstCol, never()).dropSearchIndex(any());
    verify(firstDbFirstCol, never()).createIndex(any(Bson.class), any(IndexOptions.class));
    verify(firstDbFirstCol, never()).createIndex(any(Bson.class));
  }

  @Test
  void shouldCreateIndexUsingFieldNameWithDefaultIndexValueAndDefaultOptions() throws Exception {
    // GIVEN
    ExtensionContext extensionContext = mockExtensionContext("setupFieldNameDefaultIndexValue");

    // WHEN
    tested.beforeEach(extensionContext);

    // THEN
    ArgumentCaptor<IndexOptions> optionsCaptor = ArgumentCaptor.forClass(IndexOptions.class);
    InOrder inOrder = inOrder(firstDbFirstCol);
    inOrder.verify(firstDbFirstCol).dropIndexes();
    inOrder
        .verify(firstDbFirstCol)
        .createIndex(eq(new Document("plainString", 1)), optionsCaptor.capture());

    IndexOptions capturedOptions = optionsCaptor.getValue();
    assertNull(capturedOptions.getName());
    assertFalse(capturedOptions.isUnique());
    verify(firstDbFirstCol, never()).dropSearchIndex(any());
  }

  @Test
  void shouldCreateIndexUsingFieldNameWithCustomIndexValueAndCustomIndexOptions() throws Exception {
    // GIVEN
    ExtensionContext extensionContext =
        mockExtensionContext("setupFieldNameCustomIndexValueAndOptions");

    // WHEN
    tested.beforeEach(extensionContext);

    // THEN
    ArgumentCaptor<IndexOptions> optionsCaptor = ArgumentCaptor.forClass(IndexOptions.class);
    InOrder inOrder = inOrder(firstDbFirstCol);
    inOrder.verify(firstDbFirstCol).dropIndexes();
    inOrder
        .verify(firstDbFirstCol)
        .createIndex(eq(new Document("createdAt", -1)), optionsCaptor.capture());

    IndexOptions capturedOptions = optionsCaptor.getValue();
    assertEquals("custom_unique_idx", capturedOptions.getName());
    assertTrue(capturedOptions.isUnique());
  }

  @Test
  void shouldCreateIndexUsingCustomBsonSupplierAndCustomIndexOptionsSupplier() throws Exception {
    // GIVEN
    ExtensionContext extensionContext =
        mockExtensionContext("setupCustomBsonSupplierAndOptionsSupplier");

    // WHEN
    tested.beforeEach(extensionContext);

    // THEN
    ArgumentCaptor<IndexOptions> optionsCaptor = ArgumentCaptor.forClass(IndexOptions.class);
    InOrder inOrder = inOrder(firstDbFirstCol);
    inOrder.verify(firstDbFirstCol).dropIndexes();
    inOrder
        .verify(firstDbFirstCol)
        .createIndex(
            eq(new Document("firstKey", 1).append("secondKey", -1)), optionsCaptor.capture());

    IndexOptions capturedOptions = optionsCaptor.getValue();
    assertEquals("custom_unique_idx", capturedOptions.getName());
    assertTrue(capturedOptions.isUnique());
  }

  @Test
  void shouldPrioritizeFieldNameAndIndexValueOverBsonSupplierWhenBothAreSpecified()
      throws Exception {
    // GIVEN
    ExtensionContext extensionContext = mockExtensionContext("setupBothFieldNameAndBsonSupplier");

    // WHEN
    tested.beforeEach(extensionContext);

    // THEN
    verify(firstDbFirstCol, times(1)).dropIndexes();
    verify(firstDbFirstCol, times(1))
        .createIndex(eq(new Document("priorityField", -1)), any(IndexOptions.class));
    verify(firstDbFirstCol, never())
        .createIndex(
            eq(new Document("firstKey", 1).append("secondKey", -1)), any(IndexOptions.class));
  }

  @Test
  void
      shouldGroupSearchIndexesByDatabaseAndCollectionDropIndexesOncePerCollectionAndCreateAllIndexesInOrder()
          throws Exception {
    // GIVEN
    ExtensionContext extensionContext =
        mockExtensionContext("setupGroupedMultipleCollectionsAndDatabases");

    // WHEN
    tested.beforeEach(extensionContext);

    // THEN
    InOrder col1Order = inOrder(firstDbFirstCol);
    col1Order.verify(firstDbFirstCol, times(1)).dropIndexes();
    col1Order
        .verify(firstDbFirstCol, times(1))
        .createIndex(eq(new Document("fieldA", 1)), any(IndexOptions.class));
    col1Order
        .verify(firstDbFirstCol, times(1))
        .createIndex(eq(new Document("fieldB", -1)), any(IndexOptions.class));

    InOrder col2Order = inOrder(firstDbSecondCol);
    col2Order.verify(firstDbSecondCol, times(1)).dropIndexes();
    col2Order
        .verify(firstDbSecondCol, times(1))
        .createIndex(
            eq(new Document("firstKey", 1).append("secondKey", -1)), any(IndexOptions.class));
    col2Order
        .verify(firstDbSecondCol, times(1))
        .createIndex(eq(new Document("fieldC", 1)), any(IndexOptions.class));

    verify(secondDbFirstCol, times(1)).dropIndexes();
    verify(secondDbFirstCol, never()).createIndex(any(Bson.class), any(IndexOptions.class));
  }

  @Test
  void shouldIgnoreErrorCode26WhenDroppingIndexesOnNonExistentCollectionAndStillCreateIndexes()
      throws Exception {
    // GIVEN
    MongoCommandException namespaceNotFound =
        new MongoCommandException(
            new BsonDocument("ok", new BsonInt32(0))
                .append("code", new BsonInt32(26))
                .append("codeName", new BsonString("NamespaceNotFound")),
            new ServerAddress());
    doThrow(namespaceNotFound).when(firstDbFirstCol).dropIndexes();
    ExtensionContext extensionContext = mockExtensionContext("setupFieldNameDefaultIndexValue");

    // WHEN
    tested.beforeEach(extensionContext);

    // THEN
    verify(firstDbFirstCol, times(1)).dropIndexes();
    verify(firstDbFirstCol, times(1))
        .createIndex(eq(new Document("plainString", 1)), any(IndexOptions.class));
  }

  @Test
  void shouldRethrowNon26MongoCommandExceptionWhenDroppingIndexes() throws Exception {
    // GIVEN
    MongoCommandException otherCommandException =
        new MongoCommandException(
            new BsonDocument("ok", new BsonInt32(0))
                .append("code", new BsonInt32(13))
                .append("codeName", new BsonString("Unauthorized")),
            new ServerAddress());
    doThrow(otherCommandException).when(firstDbFirstCol).dropIndexes();
    ExtensionContext extensionContext = mockExtensionContext("setupFieldNameDefaultIndexValue");

    // WHEN & THEN
    assertThrows(MongoCommandException.class, () -> tested.beforeEach(extensionContext));
  }

  private ExtensionContext mockExtensionContext(String methodName) throws NoSuchMethodException {
    ExtensionContext extensionContext = mock(ExtensionContext.class);
    when(extensionContext.getTestClass()).thenReturn(Optional.of(this.getClass()));
    when(extensionContext.getTestMethod())
        .thenReturn(Optional.of(this.getClass().getDeclaredMethod(methodName)));
    return extensionContext;
  }

  public static class CompoundBsonSupplier implements Supplier<Bson> {
    public CompoundBsonSupplier() {}

    @Override
    public Bson get() {
      return new Document("firstKey", 1).append("secondKey", -1);
    }
  }

  public static class UniqueNamedIndexOptionsSupplier implements Supplier<IndexOptions> {
    public UniqueNamedIndexOptionsSupplier() {}

    @Override
    public IndexOptions get() {
      return new IndexOptions().name("custom_unique_idx").unique(true);
    }
  }
}
