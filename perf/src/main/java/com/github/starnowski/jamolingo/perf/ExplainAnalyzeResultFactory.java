package com.github.starnowski.jamolingo.perf;

import static com.github.starnowski.jamolingo.perf.ExplainAnalyzeResult.IndexValueRepresentation.*;

import java.util.*;
import org.bson.Document;
import org.bson.conversions.Bson;

public class ExplainAnalyzeResultFactory {

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
      System.out.println("No query planner info found in explain output.");
      return null;
    }

    Document winningPlan = (Document) queryPlanner.get("winningPlan");
    String stage = winningPlan.getString("stage");

    System.out.println("Winning plan stage: " + stage);

    // Check index usage
    if ("IXSCAN".equals(stage)) {
      System.out.println("✅ Pure index scan (covered aggregation).");
      return new DefaultExplainAnalyzeResult(
          IXSCAN, resolveMatchingStages((Document) winningPlan.get("inputStage")));
    } else if ("FETCH".equals(stage)) {
      Document inputStage = (Document) winningPlan.get("inputStage");
      if (inputStage != null && "IXSCAN".equals(inputStage.getString("stage"))) {
        System.out.println("✅ Index scan with fetch (aggregation not covered, but index is used).");
        return new DefaultExplainAnalyzeResult(FETCH_IXSCAN, resolveMatchingStages(inputStage));
      }
      return new DefaultExplainAnalyzeResult(FETCH);
    } else if ("COLLSCAN".equals(stage)) {
      System.out.println("❌ Collection scan (no index used in aggregation).");
      return new DefaultExplainAnalyzeResult(COLLSCAN);
    } else {
      System.out.println("ℹ️ Other plan stage: " + stage);
    }
    return tryToResolveKnowStage(winningPlan);
  }

  private ExplainAnalyzeResult tryToResolveKnowStage(Document winningPlan) {
    String stage = winningPlan.getString("stage");
    if ("IXSCAN".equals(stage)) {
      return new DefaultExplainAnalyzeResult(IXSCAN, resolveMatchingStages((winningPlan)));
    }
    if ("COLLSCAN".equals(stage)) {
      return new DefaultExplainAnalyzeResult(COLLSCAN);
    }
    boolean fetchExists = "FETCH".equals(stage);
    if (winningPlan.containsKey("inputStage")) {
      ExplainAnalyzeResult innerStage =
          tryToResolveKnowStage((Document) winningPlan.get("inputStage"));
      if ("IXSCAN".equals(innerStage.getIndexValue().getValue())) {
        return new DefaultExplainAnalyzeResult(
            fetchExists ? FETCH_IXSCAN : IXSCAN, innerStage.getIndexMatchStages());
      }
      return innerStage;
    } else if (winningPlan.containsKey("inputStages")) {
      List<Document> inputStages = winningPlan.getList("inputStages", Document.class);
      List<ExplainAnalyzeResult> stages =
          inputStages.stream().map(this::tryToResolveKnowStage).filter(Objects::nonNull).toList();
      if (stages.stream().anyMatch(s -> "COLLSCAN".equals(s.getIndexValue().getValue()))) {
        return new DefaultExplainAnalyzeResult(COLLSCAN);
      }
      ExplainAnalyzeResult firstIXSCANStage =
          stages.stream()
              .filter(s -> "IXSCAN".equals(s.getIndexValue().getValue()))
              .findFirst()
              .orElse(null);
      if (firstIXSCANStage != null) {
        return new DefaultExplainAnalyzeResult(
            fetchExists ? FETCH_IXSCAN : IXSCAN, firstIXSCANStage.getIndexMatchStages());
      }
      System.out.println("Resolved stages are: " + stages);
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

  private List<Bson> resolveMatchingStages(Document indexScanStage) {
    try {
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
          Document matchStage = new Document("$match", andOperator);
          return List.of(matchStage);
        }
      }
    } catch (Exception ex) {
      // TODO Match Stages were not able to resolves
      ex.printStackTrace();
    }
    // TODO
    return Collections.emptyList();
  }

  private static final class DefaultExplainAnalyzeResult implements ExplainAnalyzeResult {

    private final HasIndexValue hasIndexValue;
    private final List<Bson> indexMatchStages;

    public DefaultExplainAnalyzeResult(HasIndexValue hasIndexValue) {
      this(hasIndexValue, Collections.emptyList());
    }

    public DefaultExplainAnalyzeResult(HasIndexValue hasIndexValue, List<Bson> indexMatchStages) {
      this.hasIndexValue = hasIndexValue;
      this.indexMatchStages = indexMatchStages;
    }

    @Override
    public HasIndexValue getIndexValue() {
      return hasIndexValue;
    }

    @Override
    public List<Bson> getIndexMatchStages() {
      return indexMatchStages;
    }
  }
}
