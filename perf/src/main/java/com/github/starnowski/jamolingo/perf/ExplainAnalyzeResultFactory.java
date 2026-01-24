package com.github.starnowski.jamolingo.perf;

import static com.github.starnowski.jamolingo.perf.ExplainAnalyzeResult.IndexValueRepresentation.*;

import java.util.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExplainAnalyzeResultFactory {

  private static final Logger logger = LoggerFactory.getLogger(ExplainAnalyzeResultFactory.class);

  public ExplainAnalyzeResult build(Document explain) {
    // Navigate to winning plan
    Document queryPlanner = (Document) explain.get("queryPlanner");

    if (queryPlanner == null) {
      // Extracting query plan for $cursor
      queryPlanner =
          (Document)
              Optional.ofNullable(explain.get("stages"))
                  .filter(stages -> stages instanceof List)
                  .map(stages -> (List) stages)
                  .orElse(List.of())
                  .stream()
                  .filter(i -> i instanceof Document)
                  .filter(i -> ((Document) i).containsKey("$cursor"))
                  .findFirst()
                  .map(c -> ((Document) c).get("$cursor"))
                  .or(() -> new Document())
                  .map(c -> ((Document) c).get("queryPlanner"))
                  .orElse(null);
    }

    if (queryPlanner == null) {
      logger.debug("No query planner info found in explain output.");
      return null;
    }

    Document winningPlan = (Document) queryPlanner.get("winningPlan");
    String stage = winningPlan.getString("stage");

    logger.debug("Winning plan stage: {}", stage);

    // Check index usage
    if ("IXSCAN".equals(stage)) {
      logger.debug("Pure index scan (covered aggregation).");
      return new DefaultExplainAnalyzeResult(
          IXSCAN, resolveMatchingStages((Document) winningPlan.get("inputStage")));
    } else if ("FETCH".equals(stage)) {
      Document inputStage = (Document) winningPlan.get("inputStage");
      if (inputStage != null && "IXSCAN".equals(inputStage.getString("stage"))) {
        logger.debug("Index scan with fetch (aggregation not covered, but index is used).");
        return new DefaultExplainAnalyzeResult(FETCH_IXSCAN, resolveMatchingStages(inputStage));
      }
      if (inputStage != null && "OR".equals(inputStage.getString("stage"))) {
        return tryToResolveKnowStage(winningPlan, true);
      }
      return new DefaultExplainAnalyzeResult(FETCH);
    } else if ("COLLSCAN".equals(stage)) {
      logger.debug("Collection scan (no index used in aggregation).");
      return new DefaultExplainAnalyzeResult(COLLSCAN);
    } else {
      logger.debug("Other plan stage: {}", stage);
    }
    return tryToResolveKnowStage(winningPlan);
  }

  private ExplainAnalyzeResult tryToResolveKnowStage(Document winningPlan) {
    return tryToResolveKnowStage(winningPlan, false);
  }

  private ExplainAnalyzeResult tryToResolveKnowStage(Document winningPlan, boolean mergeIndex) {
    String stage = winningPlan.getString("stage");
    if ("IXSCAN".equals(stage)) {
      return resolveExplainAnalyzeResult(
          IXSCAN, () -> resolveMatchingStages(winningPlan, mergeIndex));
    }
    if ("COLLSCAN".equals(stage)) {
      return new DefaultExplainAnalyzeResult(COLLSCAN);
    }
    boolean fetchExists = "FETCH".equals(stage);
    if (winningPlan.containsKey("inputStage")) {
      ExplainAnalyzeResult innerStage =
          tryToResolveKnowStage((Document) winningPlan.get("inputStage"), mergeIndex);
      if ("IXSCAN".equals(innerStage.getIndexValue().getValue())) {
        return new DefaultExplainAnalyzeResult(
            fetchExists ? FETCH_IXSCAN : IXSCAN,
            innerStage.getIndexMatchStages(),
            innerStage.getException());
      }
      return innerStage;
    } else if (winningPlan.containsKey("inputStages")) {
      List<Document> inputStages = winningPlan.getList("inputStages", Document.class);
      boolean orStage = "OR".equals(winningPlan.get("stage"));
      List<ExplainAnalyzeResult> stages =
          inputStages.stream()
              .map(s -> tryToResolveKnowStage(s, orStage || mergeIndex))
              .filter(Objects::nonNull)
              .toList();
      if (stages.stream().anyMatch(s -> "COLLSCAN".equals(s.getIndexValue().getValue()))) {
        return new DefaultExplainAnalyzeResult(COLLSCAN);
      }
      ExplainAnalyzeResult firstIXSCANStage =
          stages.stream()
              .filter(s -> "IXSCAN".equals(s.getIndexValue().getValue()))
              .findFirst()
              .orElse(null);
      if (firstIXSCANStage != null) {
        List<Bson> indexMatchStages = firstIXSCANStage.getIndexMatchStages();
        Throwable exception = null;
        if (orStage) {
          List<ExplainAnalyzeResult> ixscanStages =
              stages.stream().filter(s -> "IXSCAN".equals(s.getIndexValue().getValue())).toList();
          exception =
              ixscanStages.stream()
                  .map(ExplainAnalyzeResult::getException)
                  .filter(Objects::nonNull)
                  .findFirst()
                  .orElse(null);
          if (exception == null) {
            indexMatchStages =
                List.of(
                    new Document(
                        "$match",
                        new Document(
                            "$or",
                            ixscanStages.stream()
                                .flatMap(is -> is.getIndexMatchStages().stream())
                                .toList())));
          } else {
            indexMatchStages = Collections.emptyList();
          }
        }
        return new DefaultExplainAnalyzeResult(
            fetchExists ? FETCH_IXSCAN : IXSCAN, indexMatchStages, exception);
      }
      logger.debug("Resolved stages are: {}", stages);
      return stages.stream().findFirst().orElse(new DefaultExplainAnalyzeResult(null));
    }
    return new DefaultExplainAnalyzeResult(null);
  }

  private static final class RawIndexValue implements ExplainAnalyzeResult.HasIndexValue {

    private final String rawValue;

    private RawIndexValue(String rawValue) {
      this.rawValue = rawValue;
    }

    @Override
    public String getValue() {
      return rawValue;
    }
  }

  private ExplainAnalyzeResult resolveExplainAnalyzeResult(
      ExplainAnalyzeResult.IndexValueRepresentation indexValueRepresentation,
      java.util.function.Supplier<List<Bson>> matchStagesSupplier) {
    try {
      return new DefaultExplainAnalyzeResult(
          indexValueRepresentation, matchStagesSupplier.get(), null);
    } catch (Throwable ex) {
      return new DefaultExplainAnalyzeResult(
          indexValueRepresentation, Collections.emptyList(), ex);
    }
  }

  private List<Bson> resolveMatchingStages(Document indexScanStage) {
    return resolveMatchingStages(indexScanStage, false);
  }

  private List<Bson> resolveMatchingStages(Document indexScanStage, boolean mergeIndex) {
    if (indexScanStage.containsKey("indexBounds")) {
      Document indexBounds = indexScanStage.get("indexBounds", Document.class);
      List<Bson> conditions = new ArrayList<>();

      for (String key : indexBounds.keySet()) {
        if (key.startsWith("$")) {
          continue;
        }
        Object boundsObj = indexBounds.get(key);
        if (boundsObj instanceof List) {
          List<String> bounds = (List<String>) boundsObj;
          List<Object> exactMatches = new ArrayList<>();
          for (String bound : bounds) {
            try {
              org.bson.BsonArray bsonArray = org.bson.BsonArray.parse(bound);
              if (bsonArray.size() == 2 && bsonArray.get(0).equals(bsonArray.get(1))) {
                // Equality match: ["val", "val"]
                // Convert BsonValue to Java Object if possible, or keep as BsonValue
                // simple types:
                if (bsonArray.get(0).isString()) {
                  exactMatches.add(bsonArray.get(0).asString().getValue());
                } else if (bsonArray.get(0).isInt32()) {
                  exactMatches.add(bsonArray.get(0).asInt32().getValue());
                } else if (bsonArray.get(0).isInt64()) {
                  exactMatches.add(bsonArray.get(0).asInt64().getValue());
                } else if (bsonArray.get(0).isDouble()) {
                  exactMatches.add(bsonArray.get(0).asDouble().getValue());
                } else if (bsonArray.get(0).isBoolean()) {
                  exactMatches.add(bsonArray.get(0).asBoolean().getValue());
                } else if (bsonArray.get(0).isRegularExpression()) {
                  exactMatches.add(bsonArray.get(0).asRegularExpression());
                }
                // TODO handle other types if needed
              }
            } catch (Exception e) {
              // Not a parseable JSON array or other error, ignore
            }
          }
          if (!exactMatches.isEmpty()) {
            conditions.add(new Document(key, new Document("$in", exactMatches)));
          }
        }
      }

      if (!conditions.isEmpty()) {
        Document andOperator = new Document("$and", conditions);
        if (mergeIndex) {
          return List.of(andOperator);
        }
        return List.of(new Document("$match", andOperator));
      }
    }
    return Collections.emptyList();
  }

  private static final class DefaultExplainAnalyzeResult implements ExplainAnalyzeResult {

    private final HasIndexValue hasIndexValue;
    private final List<Bson> indexMatchStages;
    private final Throwable exception;

    public DefaultExplainAnalyzeResult(HasIndexValue hasIndexValue) {
      this(hasIndexValue, Collections.emptyList(), null);
    }

    public DefaultExplainAnalyzeResult(HasIndexValue hasIndexValue, List<Bson> indexMatchStages) {
      this(hasIndexValue, indexMatchStages, null);
    }

    public DefaultExplainAnalyzeResult(
        HasIndexValue hasIndexValue, List<Bson> indexMatchStages, Throwable exception) {
      this.hasIndexValue = hasIndexValue;
      this.indexMatchStages = indexMatchStages;
      this.exception = exception;
    }

    @Override
    public HasIndexValue getIndexValue() {
      return hasIndexValue;
    }

    @Override
    public List<Bson> getIndexMatchStages() {
      return indexMatchStages;
    }

    @Override
    public Throwable getException() {
      return exception;
    }
  }
}