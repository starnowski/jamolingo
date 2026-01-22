package com.github.starnowski.jamolingo.perf;

import static com.github.starnowski.jamolingo.perf.ExplainAnalyzeResult.IndexValueRepresentation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.bson.Document;

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
      return new DefaultExplainAnalyzeResult(IXSCAN);
    } else if ("FETCH".equals(stage)) {
      Document inputStage = (Document) winningPlan.get("inputStage");
      if (inputStage != null && "IXSCAN".equals(inputStage.getString("stage"))) {
        System.out.println("✅ Index scan with fetch (aggregation not covered, but index is used).");
        return new DefaultExplainAnalyzeResult(FETCH_IXSCAN);
      }
      return new DefaultExplainAnalyzeResult(FETCH);
    } else if ("COLLSCAN".equals(stage)) {
      System.out.println("❌ Collection scan (no index used in aggregation).");
      return new DefaultExplainAnalyzeResult(COLLSCAN);
    } else {
      System.out.println("ℹ️ Other plan stage: " + stage);
    }
    ExplainAnalyzeResult.HasIndexValue hasIndexValue = tryToResolveKnowStage(winningPlan);
    return new DefaultExplainAnalyzeResult(hasIndexValue);
  }

  private ExplainAnalyzeResult.HasIndexValue tryToResolveKnowStage(Document winningPlan) {
    String stage = winningPlan.getString("stage");
    if ("IXSCAN".equals(stage)) {
      return IXSCAN;
    }
    if ("COLLSCAN".equals(stage)) {
      return COLLSCAN;
    }
    boolean fetchExists = "FETCH".equals(stage);
    if (winningPlan.containsKey("inputStage")) {
      String innerStage =
          tryToResolveKnowStage((Document) winningPlan.get("inputStage")).getValue();
      if ("IXSCAN".equals(innerStage)) {
        return fetchExists ? FETCH_IXSCAN : IXSCAN;
      }
      return new RawIndexValue(innerStage);
    } else if (winningPlan.containsKey("inputStages")) {
      List<Document> inputStages = winningPlan.getList("inputStages", Document.class);
      Set<String> stages =
          inputStages.stream()
              .map(this::tryToResolveKnowStage)
              .filter(Objects::nonNull)
              .map(ExplainAnalyzeResult.HasIndexValue::getValue)
              .collect(Collectors.toUnmodifiableSet());
      if (stages.contains("COLLSCAN")) {
        return COLLSCAN;
      }
      if (stages.equals(Set.of("IXSCAN"))) {
        return fetchExists ? FETCH_IXSCAN : IXSCAN;
      }
      System.out.println("Resolved stages are: " + stages);
      return new RawIndexValue(stages.stream().findFirst().orElse(null));
    }
    return new RawIndexValue(null);
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

  private static final class DefaultExplainAnalyzeResult implements ExplainAnalyzeResult {

    private final HasIndexValue hasIndexValue;

    private DefaultExplainAnalyzeResult(HasIndexValue hasIndexValue) {
      this.hasIndexValue = hasIndexValue;
    }

    @Override
    public HasIndexValue getIndexValue() {
      return hasIndexValue;
    }
  }
}
