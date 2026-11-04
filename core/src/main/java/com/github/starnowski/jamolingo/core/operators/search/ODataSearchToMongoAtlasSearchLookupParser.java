package com.github.starnowski.jamolingo.core.operators.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.olingo.server.api.uri.queryoption.SearchOption;
import org.bson.Document;
import org.bson.conversions.Bson;

/**
 * Helper parser that creates a two-stage aggregation pipeline ($lookup with an inner $search
 * pipeline followed by a $match stage checking if at least one matching document exists) to
 * evaluate OData $search criteria when $search cannot be the first stage in the main aggregation
 * pipeline.
 */
public class ODataSearchToMongoAtlasSearchLookupParser extends ODataSearchToMongoAtlasSearchParser {

  /** MongoDB document identifier field name used for localField, foreignField, and $project. */
  public static final String ID_FIELD_NAME = "_id";

  /** Default property name under which the $lookup stage stores its result array. */
  public static final String DEFAULT_AS_PROPERTY_NAME = "metaResult";

  private final String collectionName;
  private final String asPropertyName;
  private final ODataSearchToMongoTextSearchParser<
          ODataSearchToMongoAtlasSearchOptions, ? extends SearchOperatorResult>
      searchParser;

  /**
   * Constructs a new ODataSearchToMongoAtlasSearchLookupParser using the default asPropertyName.
   *
   * @param collectionName the target collection name for the $lookup.from property
   * @param searchParser the parser component that generates the inner $search stages
   */
  public ODataSearchToMongoAtlasSearchLookupParser(
      String collectionName,
      ODataSearchToMongoTextSearchParser<
              ODataSearchToMongoAtlasSearchOptions, ? extends SearchOperatorResult>
          searchParser) {
    this(collectionName, DEFAULT_AS_PROPERTY_NAME, searchParser);
  }

  /**
   * Constructs a new ODataSearchToMongoAtlasSearchLookupParser.
   *
   * @param collectionName the target collection name for the $lookup.from property
   * @param asPropertyName the property name to store $lookup results (for $lookup.as)
   * @param searchParser the parser component that generates the inner $search stages
   */
  public ODataSearchToMongoAtlasSearchLookupParser(
      String collectionName,
      String asPropertyName,
      ODataSearchToMongoTextSearchParser<
              ODataSearchToMongoAtlasSearchOptions, ? extends SearchOperatorResult>
          searchParser) {
    super(null);
    this.collectionName = collectionName;
    this.asPropertyName =
        asPropertyName != null && !asPropertyName.isEmpty()
            ? asPropertyName
            : DEFAULT_AS_PROPERTY_NAME;
    this.searchParser = searchParser;
  }

  /**
   * Constructs a new ODataSearchToMongoAtlasSearchLookupParser using the default asPropertyName and
   * wrapping the provided SearchDocumentFactory in an ODataSearchToMongoAtlasSearchParser.
   *
   * @param collectionName the target collection name for the $lookup.from property
   * @param searchDocumentFactory the factory used to build the inner $search document
   */
  public ODataSearchToMongoAtlasSearchLookupParser(
      String collectionName, SearchDocumentFactory searchDocumentFactory) {
    this(
        collectionName,
        DEFAULT_AS_PROPERTY_NAME,
        new ODataSearchToMongoAtlasSearchParser(searchDocumentFactory));
  }

  /**
   * Constructs a new ODataSearchToMongoAtlasSearchLookupParser wrapping the provided
   * SearchDocumentFactory in an ODataSearchToMongoAtlasSearchParser.
   *
   * @param collectionName the target collection name for the $lookup.from property
   * @param asPropertyName the property name to store $lookup results (for $lookup.as)
   * @param searchDocumentFactory the factory used to build the inner $search document
   */
  public ODataSearchToMongoAtlasSearchLookupParser(
      String collectionName, String asPropertyName, SearchDocumentFactory searchDocumentFactory) {
    this(
        collectionName,
        asPropertyName,
        new ODataSearchToMongoAtlasSearchParser(searchDocumentFactory));
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
   * Gets the parser component that generates the inner $search stages.
   *
   * @return the search parser
   */
  public ODataSearchToMongoTextSearchParser<
          ODataSearchToMongoAtlasSearchOptions, ? extends SearchOperatorResult>
      getSearchParser() {
    return searchParser;
  }

  @Override
  public SearchOperatorResultForAtlasSearch parse(SearchOption searchOption) {
    return parse(searchOption, null);
  }

  @Override
  public SearchOperatorResultForAtlasSearch parse(
      SearchOption searchOption, ODataSearchToMongoAtlasSearchOptions options) {
    SearchOperatorResult searchResult =
        options != null
            ? searchParser.parse(searchOption, options)
            : searchParser.parse(searchOption);

    List<Bson> lookupPipeline = new ArrayList<>(searchResult.getStageObjects());
    lookupPipeline.add(new Document("$project", new Document(ID_FIELD_NAME, 1)));

    Document lookupInner =
        new Document("from", collectionName)
            .append("localField", ID_FIELD_NAME)
            .append("foreignField", ID_FIELD_NAME)
            .append("pipeline", lookupPipeline)
            .append("as", asPropertyName);

    Document lookupStage = new Document("$lookup", lookupInner);
    Document matchStage =
        new Document("$match", new Document(asPropertyName + ".0", new Document("$exists", true)));

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
        searchParser;

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
     * Sets the parser component that generates the inner $search stages.
     *
     * @param searchParser the search parser
     * @return the builder instance
     */
    public Builder withSearchParser(
        ODataSearchToMongoTextSearchParser<
                ODataSearchToMongoAtlasSearchOptions, ? extends SearchOperatorResult>
            searchParser) {
      this.searchParser = searchParser;
      return this;
    }

    /**
     * Sets the SearchDocumentFactory by wrapping it in an ODataSearchToMongoAtlasSearchParser.
     *
     * @param searchDocumentFactory the factory used to build the inner $search document
     * @return the builder instance
     */
    public Builder withSearchDocumentFactory(SearchDocumentFactory searchDocumentFactory) {
      this.searchParser = new ODataSearchToMongoAtlasSearchParser(searchDocumentFactory);
      return this;
    }

    /**
     * Builds a new ODataSearchToMongoAtlasSearchLookupParser instance.
     *
     * @return a new ODataSearchToMongoAtlasSearchLookupParser instance
     */
    public ODataSearchToMongoAtlasSearchLookupParser build() {
      return new ODataSearchToMongoAtlasSearchLookupParser(
          collectionName, asPropertyName, searchParser);
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
