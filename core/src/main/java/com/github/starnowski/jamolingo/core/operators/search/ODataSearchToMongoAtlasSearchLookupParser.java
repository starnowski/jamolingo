package com.github.starnowski.jamolingo.core.operators.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.olingo.server.api.uri.queryoption.SearchOption;
import org.bson.Document;
import org.bson.conversions.Bson;

/**
 * Helper parser that creates a two-stage aggregation pipeline ($lookup with an inner $searchMeta
 * stage followed by a $match stage checking if the total count is greater than 0) to evaluate OData
 * $search criteria when $search cannot be the first stage in the main aggregation pipeline.
 */
public class ODataSearchToMongoAtlasSearchLookupParser extends ODataSearchToMongoAtlasSearchParser {

  /** Variable name defined in the $lookup let clause referencing the document _id. */
  public static final String TARGET_ID_VARIABLE_NAME = "targetId";

  /** MongoDB document identifier field name used for localField, foreignField, and let variable. */
  public static final String ID_FIELD_NAME = "_id";

  /** Default property name under which the $lookup stage stores its result array. */
  public static final String DEFAULT_AS_PROPERTY_NAME = "metaResult";

  private final String collectionName;
  private final String asPropertyName;
  private final ODataSearchToMongoTextSearchParser<
          ODataSearchToMongoAtlasSearchOptions, ? extends SearchOperatorResult>
      searchMetaParser;

  /**
   * Constructs a new ODataSearchToMongoAtlasSearchLookupParser using the default asPropertyName.
   *
   * @param collectionName the target collection name for the $lookup.from property
   * @param searchMetaParser the parser component that generates the $searchMeta stage
   */
  public ODataSearchToMongoAtlasSearchLookupParser(
      String collectionName,
      ODataSearchToMongoTextSearchParser<
              ODataSearchToMongoAtlasSearchOptions, ? extends SearchOperatorResult>
          searchMetaParser) {
    this(collectionName, DEFAULT_AS_PROPERTY_NAME, searchMetaParser);
  }

  /**
   * Constructs a new ODataSearchToMongoAtlasSearchLookupParser.
   *
   * @param collectionName the target collection name for the $lookup.from property
   * @param asPropertyName the property name to store $lookup results (for $lookup.as)
   * @param searchMetaParser the parser component that generates the $searchMeta stage
   */
  public ODataSearchToMongoAtlasSearchLookupParser(
      String collectionName,
      String asPropertyName,
      ODataSearchToMongoTextSearchParser<
              ODataSearchToMongoAtlasSearchOptions, ? extends SearchOperatorResult>
          searchMetaParser) {
    super(null);
    this.collectionName = collectionName;
    this.asPropertyName =
        asPropertyName != null && !asPropertyName.isEmpty()
            ? asPropertyName
            : DEFAULT_AS_PROPERTY_NAME;
    this.searchMetaParser = searchMetaParser;
  }

  /**
   * Constructs a new ODataSearchToMongoAtlasSearchLookupParser using the default asPropertyName and
   * wrapping the provided SearchDocumentFactory in an ODataSearchToMongoAtlasSearchMetaParser.
   *
   * @param collectionName the target collection name for the $lookup.from property
   * @param searchDocumentFactory the factory used to build the inner $searchMeta document
   */
  public ODataSearchToMongoAtlasSearchLookupParser(
      String collectionName, SearchDocumentFactory searchDocumentFactory) {
    this(
        collectionName,
        DEFAULT_AS_PROPERTY_NAME,
        new ODataSearchToMongoAtlasSearchMetaParser(searchDocumentFactory));
  }

  /**
   * Constructs a new ODataSearchToMongoAtlasSearchLookupParser wrapping the provided
   * SearchDocumentFactory in an ODataSearchToMongoAtlasSearchMetaParser.
   *
   * @param collectionName the target collection name for the $lookup.from property
   * @param asPropertyName the property name to store $lookup results (for $lookup.as)
   * @param searchDocumentFactory the factory used to build the inner $searchMeta document
   */
  public ODataSearchToMongoAtlasSearchLookupParser(
      String collectionName, String asPropertyName, SearchDocumentFactory searchDocumentFactory) {
    this(
        collectionName,
        asPropertyName,
        new ODataSearchToMongoAtlasSearchMetaParser(searchDocumentFactory));
  }

  /**
   * Gets the target collection name used for the $lookup.from property.
   *
   * @return the collection name
   */
  public String getCollectionName() {
    return collectionName;
  }

  /**
   * Gets the property name used for the $lookup.as property.
   *
   * @return the property name where lookup results are stored
   */
  public String getAsPropertyName() {
    return asPropertyName;
  }

  /**
   * Gets the parser component that generates the $searchMeta stage.
   *
   * @return the searchMeta parser
   */
  public ODataSearchToMongoTextSearchParser<
          ODataSearchToMongoAtlasSearchOptions, ? extends SearchOperatorResult>
      getSearchMetaParser() {
    return searchMetaParser;
  }

  @Override
  public SearchOperatorResultForAtlasSearch parse(SearchOption searchOption) {
    return parse(searchOption, null);
  }

  @Override
  public SearchOperatorResultForAtlasSearch parse(
      SearchOption searchOption, ODataSearchToMongoAtlasSearchOptions options) {
    SearchOperatorResult searchMetaResult =
        options != null
            ? searchMetaParser.parse(searchOption, options)
            : searchMetaParser.parse(searchOption);

    List<Bson> lookupPipeline = new ArrayList<>(searchMetaResult.getStageObjects());

    Document lookupInner =
        new Document("from", collectionName)
            .append("localField", ID_FIELD_NAME)
            .append("foreignField", ID_FIELD_NAME)
            .append("let", new Document(TARGET_ID_VARIABLE_NAME, "$" + ID_FIELD_NAME))
            .append("pipeline", lookupPipeline)
            .append("as", asPropertyName);

    Document lookupStage = new Document("$lookup", lookupInner);
    Document matchStage =
        new Document(
            "$match", new Document(asPropertyName + ".count.total", new Document("$gt", 0)));

    List<Bson> stages = List.of(lookupStage, matchStage);
    return new DefaultLookupSearchOperatorResult(stages, asPropertyName);
  }

  /**
   * Returns a new builder for ODataSearchToMongoAtlasSearchLookupParser.
   *
   * @return a new builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  /** Builder for ODataSearchToMongoAtlasSearchLookupParser. */
  public static class Builder {

    private String collectionName;
    private String asPropertyName = DEFAULT_AS_PROPERTY_NAME;
    private ODataSearchToMongoTextSearchParser<
            ODataSearchToMongoAtlasSearchOptions, ? extends SearchOperatorResult>
        searchMetaParser;

    /**
     * Sets the target collection name for the $lookup.from property.
     *
     * @param collectionName the collection name
     * @return the builder instance
     */
    public Builder withCollectionName(String collectionName) {
      this.collectionName = collectionName;
      return this;
    }

    /**
     * Sets the property name to store the $lookup results (for $lookup.as).
     *
     * @param asPropertyName the property name
     * @return the builder instance
     */
    public Builder withAsPropertyName(String asPropertyName) {
      this.asPropertyName = asPropertyName;
      return this;
    }

    /**
     * Sets the parser component that generates the $searchMeta stage.
     *
     * @param searchMetaParser the searchMeta parser
     * @return the builder instance
     */
    public Builder withSearchMetaParser(
        ODataSearchToMongoTextSearchParser<
                ODataSearchToMongoAtlasSearchOptions, ? extends SearchOperatorResult>
            searchMetaParser) {
      this.searchMetaParser = searchMetaParser;
      return this;
    }

    /**
     * Sets the SearchDocumentFactory by wrapping it in an ODataSearchToMongoAtlasSearchMetaParser.
     *
     * @param searchDocumentFactory the factory used to build the inner $searchMeta document
     * @return the builder instance
     */
    public Builder withSearchDocumentFactory(SearchDocumentFactory searchDocumentFactory) {
      this.searchMetaParser = new ODataSearchToMongoAtlasSearchMetaParser(searchDocumentFactory);
      return this;
    }

    /**
     * Builds a new ODataSearchToMongoAtlasSearchLookupParser instance.
     *
     * @return a new ODataSearchToMongoAtlasSearchLookupParser instance
     */
    public ODataSearchToMongoAtlasSearchLookupParser build() {
      return new ODataSearchToMongoAtlasSearchLookupParser(
          collectionName, asPropertyName, searchMetaParser);
    }
  }

  private static class DefaultLookupSearchOperatorResult
      implements SearchOperatorResultForAtlasSearch {

    private final List<Bson> stageObjects;
    private final String asPropertyName;

    private DefaultLookupSearchOperatorResult(List<Bson> stages, String asPropertyName) {
      this.stageObjects = Collections.unmodifiableList(stages);
      this.asPropertyName = asPropertyName;
    }

    @Override
    public List<Bson> getStageObjects() {
      return stageObjects;
    }

    @Override
    public List<Bson> getSearchStages() {
      return stageObjects;
    }

    @Override
    public List<Bson> getScoreFilterStages() {
      return Collections.emptyList();
    }

    @Override
    public List<String> getUsedMongoDocumentProperties() {
      return List.of(ID_FIELD_NAME);
    }

    @Override
    public List<String> getWrittenMongoDocumentProperties() {
      return List.of(asPropertyName);
    }

    @Override
    public List<String> getAddedMongoDocumentProperties() {
      return List.of(asPropertyName);
    }

    @Override
    public List<String> getRemovedMongoDocumentProperties() {
      return List.of();
    }

    @Override
    public boolean isDocumentShapeRedefined() {
      return false;
    }
  }
}
