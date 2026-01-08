


### How to test generate $match stage for indexed fields

$indexMath = resolveBasedOnExplainResultMathStageThatUseIndex(pipeline)

##### 1
given:
    $indexMath = resolveBasedOnExplainResultMathStageThatUseIndex(pipeline)

when:
    def explainResult = execute($indexMath)

then:
    // explainResult should index

##### 2 verity that results for original pipeline and pipeline with added resolved index match stage is the same
given:
    $indexMath = resolveBasedOnExplainResultMathStageThatUseIndex(pipeline)
    resultForOriginal = execute(pipeline)

when:
    def resultForModifiedPipeline = execute($indexMath + pipeline)

then:
    // resultForOriginal and resultForModifiedPipeline should be the same

##### 3 similar as 2 case but the enhance pipeline would contain after $indexMath stage the $set stage that set dummy property and at the end removes this dummy property
given:
$indexMath = resolveBasedOnExplainResultMathStageThatUseIndex(pipeline)
resultForOriginal = execute(pipeline)
$set = {
   "$set": { "dummyProp": 1 }
}

$unset = {
    "$unset": "dummyProp"
}

when:
// We add the $set to make sure that Query Planner won't decide to optimize somehow the $match stages so that it would not use $indexMath
def resultForModifiedPipeline = execute($indexMath + $set + pipeline + $unset)

then:
// resultForOriginal and resultForModifiedPipeline should be the same