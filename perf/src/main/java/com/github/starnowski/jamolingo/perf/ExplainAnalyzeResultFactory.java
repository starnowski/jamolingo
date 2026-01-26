package com.github.starnowski.jamolingo.perf;

import static com.github.starnowski.jamolingo.perf.ExplainAnalyzeResult.IndexValueRepresentation.*;

import java.util.*;
import org.bson.*;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Factory for creating {@link ExplainAnalyzeResult} instances from MongoDB explain results. */
public class ExplainAnalyzeResultFactory {

  private static final Logger logger = LoggerFactory.getLogger(ExplainAnalyzeResultFactory.class);

  private static final List<Bson> MATCH_2DSPHERE_STage = null;

  /**
   * Builds an {@link ExplainAnalyzeResult} from the provided MongoDB explain document.
   *
   * @param explain the MongoDB explain document
   * @return the explain analyze result, or null if the query planner info is missing
   */
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
          IXSCAN,
          resolveMatchingStages(
              (Document) winningPlan.get("inputStage"),
              (Document) winningPlan.get("filter"),
              false));
    } else if ("FETCH".equals(stage)) {
      Document inputStage = (Document) winningPlan.get("inputStage");
      if (inputStage != null && "IXSCAN".equals(inputStage.getString("stage"))) {
        logger.debug("Index scan with fetch (aggregation not covered, but index is used).");
        return new DefaultExplainAnalyzeResult(
            FETCH_IXSCAN,
            resolveMatchingStages(inputStage, (Document) winningPlan.get("filter"), false));
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
          IXSCAN,
          () ->
              resolveMatchingStages(winningPlan, (Document) winningPlan.get("filter"), mergeIndex));
    }
    if ("COLLSCAN".equals(stage)) {
      return new DefaultExplainAnalyzeResult(COLLSCAN);
    }
    boolean fetchExists = "FETCH".equals(stage);
    if (winningPlan.containsKey("inputStage")) {
      Document inputStageDoc = (Document) winningPlan.get("inputStage");
      if (fetchExists && "IXSCAN".equals(inputStageDoc.getString("stage"))) {
        return resolveExplainAnalyzeResult(
            FETCH_IXSCAN,
            () ->
                resolveMatchingStages(
                    inputStageDoc, (Document) winningPlan.get("filter"), mergeIndex));
      }
      ExplainAnalyzeResult innerStage = tryToResolveKnowStage(inputStageDoc, mergeIndex);
      if (innerStage != null
          && innerStage.getIndexValue() != null
          && innerStage.getIndexValue().getValue().contains("IXSCAN")) {
        return new DefaultExplainAnalyzeResult(
            fetchExists ? FETCH_IXSCAN : innerStage.getIndexValue(),
            innerStage.getIndexMatchStages(),
            innerStage.getResolutionIndexMatchStagesException());
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
              .filter(
                  s -> s.getIndexValue() != null && s.getIndexValue().getValue().contains("IXSCAN"))
              .findFirst()
              .orElse(null);
      if (firstIXSCANStage != null) {
        List<Bson> indexMatchStages = firstIXSCANStage.getIndexMatchStages();
        Throwable exception = null;
        if (orStage) {
          List<ExplainAnalyzeResult> ixscanStages =
              stages.stream()
                  .filter(
                      s ->
                          s.getIndexValue() != null
                              && s.getIndexValue().getValue().contains("IXSCAN"))
                  .toList();
          exception =
              ixscanStages.stream()
                  .map(ExplainAnalyzeResult::getResolutionIndexMatchStagesException)
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
                                .map(
                                    im -> {
                                      if (im instanceof Document) {
                                        Document dim = (Document) im;
                                        if (dim.containsKey("$match")) {
                                          return dim.get("$match");
                                        }
                                      }
                                      return im;
                                    })
                                .toList())));
          } else {
            indexMatchStages = Collections.emptyList();
          }
        }
        return new DefaultExplainAnalyzeResult(
            fetchExists ? FETCH_IXSCAN : firstIXSCANStage.getIndexValue(),
            indexMatchStages,
            exception);
      }
      logger.debug("Resolved stages are: {}", stages);
      return stages.stream().findFirst().orElse(new DefaultExplainAnalyzeResult(null));
    }
    return new DefaultExplainAnalyzeResult(null);
  }

  private Document extractCondition(Document filter, String key) {
    if (filter == null) return null;
    if (filter.containsKey(key)) return (Document) filter.get(key);
    if (filter.containsKey("$or")) {
      List<Document> orList = filter.getList("$or", Document.class);
      if (orList.size() == 1) return extractCondition(orList.get(0), key);
    }
    if (filter.containsKey("$and")) {
      List<Document> andList = filter.getList("$and", Document.class);
      for (Document andBranch : andList) {
        Document cond = extractCondition(andBranch, key);
        if (cond != null) return cond;
      }
    }
    return null;
  }

  private ExplainAnalyzeResult resolveExplainAnalyzeResult(
      ExplainAnalyzeResult.IndexValueRepresentation indexValueRepresentation,
      java.util.function.Supplier<List<Bson>> matchStagesSupplier) {
    try {
      return new DefaultExplainAnalyzeResult(
          indexValueRepresentation, matchStagesSupplier.get(), null);
    } catch (Throwable ex) {
      return new DefaultExplainAnalyzeResult(indexValueRepresentation, Collections.emptyList(), ex);
    }
  }

  private boolean startWithInfinityKey(String range) {
    return range.startsWith("(-inf.0") || range.startsWith("[-inf.0");
  }

  private boolean endsWithInfinityKey(String range) {
    return range.endsWith("inf.0)") || range.endsWith("inf.0]");
  }

  private List<Bson> resolveMatchingStages(
      Document indexScanStage, Document filter, boolean mergeIndex) {
    if (indexScanStage.containsKey("indexBounds")) {
      Document indexBounds = indexScanStage.get("indexBounds", Document.class);
      List<Bson> conditions = new ArrayList<>();
      Document keyPattern = indexScanStage.get("keyPattern", Document.class);
      boolean processExactAndRangeKeys = true;
      if (keyPattern != null && filter != null) {
        for (String key : keyPattern.keySet()) {
          if ("2dsphere".equals(keyPattern.get(key)) || "2d".equals(keyPattern.get(key))) {
            Document condition = extractCondition(filter, key);
            if (condition != null) {
              conditions.add(new Document(key, condition));
              processExactAndRangeKeys = false;
            }
          }
        }
      }
      if (processExactAndRangeKeys) {
        for (String key : indexBounds.keySet()) {
          if (key.startsWith("$")) {
            continue;
          }
          Object boundsObj = indexBounds.get(key);
          if (boundsObj instanceof List) {
            List<?> bounds = (List<?>) boundsObj;
            List<Object> exactMatches = new ArrayList<>();
            List<Document> keyRanges = new ArrayList<>();

            String logParsedBound = "";
            for (Object boundObj : bounds) {
              try {
                if (boundObj instanceof String) {
                  String bound = (String) boundObj;
                  String parsableBound = bound;
                  boolean startInclusive = true;
                  boolean endInclusive = true;
                  if (bound.startsWith("(") || bound.startsWith("[")) {
                    if (bound.startsWith("(")) startInclusive = false;
                    parsableBound = "[" + bound.substring(1);
                  }
                  if (bound.endsWith(")") || bound.endsWith("]")) {
                    if (bound.endsWith(")")) endInclusive = false;
                    parsableBound = parsableBound.substring(0, parsableBound.length() - 1) + "]";
                  }

                  logParsedBound = parsableBound;
                  boolean minInfinity = startWithInfinityKey(parsableBound);
                  boolean maxInfinity = endsWithInfinityKey(parsableBound);
                  if (minInfinity) {
                    parsableBound = parsableBound.replace("[-inf.0", "[0");
                  }
                  if (maxInfinity) {
                    parsableBound = parsableBound.replace("inf.0]", "0]");
                  }
                  org.bson.BsonArray bsonArray = org.bson.BsonArray.parse(parsableBound);
                  if (minInfinity) {
                    bsonArray.set(0, new BsonMinKey());
                  }
                  if (maxInfinity) {
                    bsonArray.set(1, new BsonMaxKey());
                  }
                  if (bsonArray.size() == 2) {
                    BsonValue min = bsonArray.get(0);
                    BsonValue max = bsonArray.get(1);

                    if (min.equals(max) && startInclusive && endInclusive) {
                      Object val = convert(min);
                      if (val != null) exactMatches.add(val);
                    } else {
                      Document rangeDoc = new Document();
                      if (!isMinInfinity(min)) {
                        Object val = convert(min);
                        if (val != null) {
                          rangeDoc.append(startInclusive ? "$gte" : "$gt", val);
                        }
                      }
                      if (!isMaxInfinity(max)) {
                        Object val = convert(max);
                        if (val != null) {
                          rangeDoc.append(endInclusive ? "$lte" : "$lt", val);
                        }
                      }
                      if (!rangeDoc.isEmpty()) {
                        keyRanges.add(new Document(key, rangeDoc));
                      }
                    }
                  }
                } else if (boundObj instanceof Number || boundObj instanceof Boolean) {
                  exactMatches.add(boundObj);
                }
              } catch (Exception e) {
                logger.debug("Failed to parse index bound: " + boundObj, e);
              }
            }
            List<Document> keyConstraints = new ArrayList<>();
            if (!exactMatches.isEmpty()) {
              keyConstraints.add(new Document(key, new Document("$in", exactMatches)));
            }
            keyConstraints.addAll(keyRanges);

            if (keyConstraints.size() == 1) {
              conditions.add(keyConstraints.get(0));
            } else if (keyConstraints.size() > 1) {
              conditions.add(new Document("$or", keyConstraints));
            }
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

  private Object convert(BsonValue value) {
    if (value.isString()) return value.asString().getValue();
    if (value.isInt32()) return value.asInt32().getValue();
    if (value.isInt64()) return value.asInt64().getValue();
    if (value.isDouble()) return value.asDouble().getValue();
    if (value.isBoolean()) return value.asBoolean().getValue();
    if (value.isRegularExpression()) return value.asRegularExpression();
    return null;
  }

  private boolean isMinInfinity(BsonValue value) {
    if (value.getBsonType() == BsonType.MIN_KEY) return true;
    if (value.isDouble() && value.asDouble().getValue() == Double.NEGATIVE_INFINITY) return true;
    if (value.isString()) {
      String s = value.asString().getValue();
      return "-inf.0".equals(s) || "-Infinity".equals(s);
    }
    return false;
  }

  private boolean isMaxInfinity(BsonValue value) {
    if (value.getBsonType() == BsonType.MAX_KEY) return true;
    if (value.isDouble() && value.asDouble().getValue() == Double.POSITIVE_INFINITY) return true;
    if (value.isString()) {
      String s = value.asString().getValue();
      return "inf.0".equals(s) || "Infinity".equals(s);
    }
    return false;
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
    public Throwable getResolutionIndexMatchStagesException() {
      return exception;
    }
  }
}
