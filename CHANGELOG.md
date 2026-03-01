# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

* [Unreleased](#unreleased)
* [0.5.0](#050---2026-03-01)
* [0.4.1](#041---2026-01-26)
* [0.4.0](#040---2026-01-24)
* [0.3.0](#030---2026-01-21)
* [0.2.0](#020---2026-01-20)
* [0.1.0](#010---2026-01-18)

## [Unreleased]

## [0.5.0] - 2026-03-01
 
### Added

#### Core Module
*   Added support for `$filter` operator translation to MongoDB `$match` stage ([#17](https://github.com/starnowski/jamolingo/issues/17))
*   Added support for OData comparison operators: `eq`, `ne`, `gt`, `ge`, `lt`, `le`, `in` ([#17](https://github.com/starnowski/jamolingo/issues/17))
*   Added support for OData logical operators: `and`, `or`, `not` ([#17](https://github.com/starnowski/jamolingo/issues/17))
*   Added support for OData collection operators: `any()`, `all()` ([#17](https://github.com/starnowski/jamolingo/issues/17))
*   Added support for OData string functions: `tolower`, `toupper`, `trim`, `contains`, `startswith`, `endswith`, `length` ([#17](https://github.com/starnowski/jamolingo/issues/17))
*   Added support for OData math functions: `floor`, `ceiling`, `round` ([#17](https://github.com/starnowski/jamolingo/issues/17))
*   Added support for OData date and time functions: `year`, `month`, `day`, `hour`, `minute`, `second` ([#17](https://github.com/starnowski/jamolingo/issues/17))
*   `com.github.starnowski.jamolingo.core.operators.filter.FilterOperatorResult` ([#17](https://github.com/starnowski/jamolingo/issues/17))
*   `com.github.starnowski.jamolingo.core.operators.filter.ODataFilterToMongoMatchParser` ([#17](https://github.com/starnowski/jamolingo/issues/17))
*   `com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver` ([#17](https://github.com/starnowski/jamolingo/issues/17))
*   Added support for MongoDB field renaming and wrapper objects in filter mappings ([#17](https://github.com/starnowski/jamolingo/issues/17))

#### Compatibility Driver 5.x Module
*   Added integration tests for OData `$filter` operators ([#17](https://github.com/starnowski/jamolingo/issues/17))

### Changed

#### Core Module
*   Updated `com.github.starnowski.jamolingo.core.context.DefaultEdmMongoContextFacade` to implement `com.github.starnowski.jamolingo.core.api.EdmPropertyMongoPathResolver` ([#17](https://github.com/starnowski/jamolingo/issues/17))
*   Added `resolveMongoPathForEDMPath(String edmPath)` method to `com.github.starnowski.jamolingo.core.context.DefaultEdmMongoContextFacade` ([#17](https://github.com/starnowski/jamolingo/issues/17))

## [0.4.1] - 2026-01-26

### Added

*   Added support for 2d and 2dsphere indexes in `com.github.starnowski.jamolingo.perf.ExplainAnalyzeResultFactory` ([#18](https://github.com/starnowski/jamolingo/issues/18))
*   Added support for range filtering in `com.github.starnowski.jamolingo.perf.ExplainAnalyzeResultFactory` ([#18](https://github.com/starnowski/jamolingo/issues/18))


## [0.4.0] - 2026-01-24

### Added

#### Perf Module
*   `com.github.starnowski.jamolingo.perf.ExplainAnalyzeResult` ([#13](https://github.com/starnowski/jamolingo/issues/13))
*   `com.github.starnowski.jamolingo.perf.ExplainAnalyzeResultFactory` ([#13](https://github.com/starnowski/jamolingo/issues/13))

## [0.3.0] - 2026-01-21

### Changed

#### Core Module
*   Moved `com.github.starnowski.jamolingo.context.EdmMongoContextFacade` to `com.github.starnowski.jamolingo.core.api.EdmMongoContextFacade` ([#14](https://github.com/starnowski/jamolingo/issues/14))
*   Moved `com.github.starnowski.jamolingo.context.DefaultEdmMongoContextFacade` to `com.github.starnowski.jamolingo.core.api.DefaultEdmMongoContextFacade` ([#14](https://github.com/starnowski/jamolingo/issues/14))
*   Moved `com.github.starnowski.jamolingo.context.DefaultEdmPathContextSearch` to `com.github.starnowski.jamolingo.core.context.DefaultEdmPathContextSearch` ([#14](https://github.com/starnowski/jamolingo/issues/14))
*   Moved `com.github.starnowski.jamolingo.context.DefaultEntityPropertiesMongoPathContext` to `com.github.starnowski.jamolingo.core.context.DefaultEntityPropertiesMongoPathContext` ([#14](https://github.com/starnowski/jamolingo/issues/14))
*   Moved `com.github.starnowski.jamolingo.context.EdmPathContextSearch` to `com.github.starnowski.jamolingo.core.context.EdmPathContextSearch` ([#14](https://github.com/starnowski/jamolingo/issues/14))
*   Moved `com.github.starnowski.jamolingo.context.EntityPropertiesMongoPathContext` to `com.github.starnowski.jamolingo.core.context.EntityPropertiesMongoPathContext` ([#14](https://github.com/starnowski/jamolingo/issues/14))
*   Moved `com.github.starnowski.jamolingo.context.EntityPropertiesMongoPathContextBuilder` to `com.github.starnowski.jamolingo.core.context.EntityPropertiesMongoPathContextBuilder` ([#14](https://github.com/starnowski/jamolingo/issues/14))
*   Moved `com.github.starnowski.jamolingo.context.MongoPathEntry` to `com.github.starnowski.jamolingo.core.context.MongoPathEntry` ([#14](https://github.com/starnowski/jamolingo/issues/14))
*   Moved `com.github.starnowski.jamolingo.context.MongoPathResolution` to `com.github.starnowski.jamolingo.core.context.MongoPathResolution` ([#14](https://github.com/starnowski/jamolingo/issues/14))
*   Moved `com.github.starnowski.jamolingo.context.EntityMapping` to `com.github.starnowski.jamolingo.core.mapping.EntityMapping` ([#14](https://github.com/starnowski/jamolingo/issues/14))
*   Moved `com.github.starnowski.jamolingo.context.ODataMongoMapping` to `com.github.starnowski.jamolingo.core.mapping.ODataMongoMapping` ([#14](https://github.com/starnowski/jamolingo/issues/14))
*   Moved `com.github.starnowski.jamolingo.context.ODataMongoMappingFactory` to `com.github.starnowski.jamolingo.core.mapping.ODataMongoMappingFactory` ([#14](https://github.com/starnowski/jamolingo/issues/14))
*   Moved `com.github.starnowski.jamolingo.context.PropertyMapping` to `com.github.starnowski.jamolingo.core.mapping.PropertyMapping` ([#14](https://github.com/starnowski/jamolingo/issues/14))
*   Moved `com.github.starnowski.jamolingo.context.CircularReferenceMapping` to `com.github.starnowski.jamolingo.core.mapping.CircularReferenceMapping` ([#14](https://github.com/starnowski/jamolingo/issues/14))
*   Moved `com.github.starnowski.jamolingo.context.CircularReferenceMappingRecord` to `com.github.starnowski.jamolingo.core.mapping.CircularReferenceMappingRecord` ([#14](https://github.com/starnowski/jamolingo/issues/14))
*   Moved `com.github.starnowski.jamolingo.context.CircularStrategy` to `com.github.starnowski.jamolingo.core.mapping.CircularStrategy` ([#14](https://github.com/starnowski/jamolingo/issues/14))
*   Moved `com.github.starnowski.jamolingo.orderby.OdataOrderByToMongoSortParser` to `com.github.starnowski.jamolingo.core.operators.orderby.OdataOrderByToMongoSortParser` ([#14](https://github.com/starnowski/jamolingo/issues/14))
*   Moved `com.github.starnowski.jamolingo.orderby.OrderByOperatorResult` to `com.github.starnowski.jamolingo.core.operators.orderby.OrderByOperatorResult` ([#14](https://github.com/starnowski/jamolingo/issues/14))
*   Moved `com.github.starnowski.jamolingo.select.OdataSelectToMongoProjectParser` to `com.github.starnowski.jamolingo.core.operators.select.OdataSelectToMongoProjectParser` ([#14](https://github.com/starnowski/jamolingo/issues/14))
*   Moved `com.github.starnowski.jamolingo.select.SelectOperatorResult` to `com.github.starnowski.jamolingo.core.operators.select.SelectOperatorResult` ([#14](https://github.com/starnowski/jamolingo/issues/14))
*   Moved `com.github.starnowski.jamolingo.select.OlingoOperatorResult` to `com.github.starnowski.jamolingo.core.operators.OlingoOperatorResult` ([#14](https://github.com/starnowski/jamolingo/issues/14))
*   Moved `com.github.starnowski.jamolingo.skip.OdataSkipToMongoSkipParser` to `com.github.starnowski.jamolingo.core.operators.skip.OdataSkipToMongoSkipParser` ([#14](https://github.com/starnowski/jamolingo/issues/14))
*   Moved `com.github.starnowski.jamolingo.skip.SkipOperatorResult` to `com.github.starnowski.jamolingo.core.operators.skip.SkipOperatorResult` ([#14](https://github.com/starnowski/jamolingo/issues/14))
*   Moved `com.github.starnowski.jamolingo.top.OdataTopToMongoLimitParser` to `com.github.starnowski.jamolingo.core.operators.top.OdataTopToMongoLimitParser` ([#14](https://github.com/starnowski/jamolingo/issues/14))
*   Moved `com.github.starnowski.jamolingo.top.TopOperatorResult` to `com.github.starnowski.jamolingo.core.operators.top.TopOperatorResult` ([#14](https://github.com/starnowski/jamolingo/issues/14))
*   Moved `com.github.starnowski.jamolingo.context.Constants` to `com.github.starnowski.jamolingo.core.utils.Constants` ([#14](https://github.com/starnowski/jamolingo/issues/14))

## [0.2.0] - 2026-01-20

### Added

#### Core Module
*   `com.github.starnowski.jamolingo.orderby.OdataOrderByToMongoSortParser` ([#11](https://github.com/starnowski/jamolingo/issues/11))
*   `com.github.starnowski.jamolingo.orderby.OrderByOperatorResult` ([#11](https://github.com/starnowski/jamolingo/issues/11))
*   `com.github.starnowski.jamolingo.skip.OdataSkipToMongoSkipParser` ([#9](https://github.com/starnowski/jamolingo/issues/9))
*   `com.github.starnowski.jamolingo.skip.SkipOperatorResult` ([#9](https://github.com/starnowski/jamolingo/issues/9))
*   `com.github.starnowski.jamolingo.top.OdataTopToMongoLimitParser` ([#7](https://github.com/starnowski/jamolingo/issues/7))
*   `com.github.starnowski.jamolingo.top.TopOperatorResult` ([#7](https://github.com/starnowski/jamolingo/issues/7))

## [0.1.0] - 2026-01-18

### Added

#### Core Module
*   `com.github.starnowski.jamolingo.context.CircularReferenceMapping` ([#1](https://github.com/starnowski/jamolingo/issues/1))
*   `com.github.starnowski.jamolingo.context.CircularReferenceMappingRecord` ([#1](https://github.com/starnowski/jamolingo/issues/1))
*   `com.github.starnowski.jamolingo.context.CircularStrategy` ([#1](https://github.com/starnowski/jamolingo/issues/1))
*   `com.github.starnowski.jamolingo.context.Constants` ([#1](https://github.com/starnowski/jamolingo/issues/1))
*   `com.github.starnowski.jamolingo.context.DefaultEdmMongoContextFacade` ([#1](https://github.com/starnowski/jamolingo/issues/1))
*   `com.github.starnowski.jamolingo.context.DefaultEdmPathContextSearch` ([#1](https://github.com/starnowski/jamolingo/issues/1))
*   `com.github.starnowski.jamolingo.context.DefaultEntityPropertiesMongoPathContext` ([#1](https://github.com/starnowski/jamolingo/issues/1))
*   `com.github.starnowski.jamolingo.context.EdmMongoContextFacade` ([#1](https://github.com/starnowski/jamolingo/issues/1))
*   `com.github.starnowski.jamolingo.context.EdmPathContextSearch` ([#1](https://github.com/starnowski/jamolingo/issues/1))
*   `com.github.starnowski.jamolingo.context.EntityMapping` ([#1](https://github.com/starnowski/jamolingo/issues/1))
*   `com.github.starnowski.jamolingo.context.EntityPropertiesMongoPathContext` ([#1](https://github.com/starnowski/jamolingo/issues/1))
*   `com.github.starnowski.jamolingo.context.EntityPropertiesMongoPathContextBuilder` ([#1](https://github.com/starnowski/jamolingo/issues/1))
*   `com.github.starnowski.jamolingo.context.MongoPathEntry` ([#1](https://github.com/starnowski/jamolingo/issues/1))
*   `com.github.starnowski.jamolingo.context.MongoPathResolution` ([#1](https://github.com/starnowski/jamolingo/issues/1))
*   `com.github.starnowski.jamolingo.context.ODataMongoMapping` ([#1](https://github.com/starnowski/jamolingo/issues/1))
*   `com.github.starnowski.jamolingo.context.ODataMongoMappingFactory` ([#1](https://github.com/starnowski/jamolingo/issues/1))
*   `com.github.starnowski.jamolingo.context.PropertyMapping` ([#1](https://github.com/starnowski/jamolingo/issues/1))
*   `com.github.starnowski.jamolingo.select.OdataSelectToMongoProjectParser` ([#1](https://github.com/starnowski/jamolingo/issues/1))
*   `com.github.starnowski.jamolingo.select.OlingoOperatorResult` ([#1](https://github.com/starnowski/jamolingo/issues/1))
*   `com.github.starnowski.jamolingo.select.SelectOperatorResult` ([#1](https://github.com/starnowski/jamolingo/issues/1))

#### Common JSON Module
*   `com.github.starnowski.jamolingo.common.json.JSONOverrideHelper` ([#1](https://github.com/starnowski/jamolingo/issues/1))
*   `com.github.starnowski.jamolingo.common.json.PatchHelper` ([#1](https://github.com/starnowski/jamolingo/issues/1))

#### JUnit 5 Mongo Extension
*   `com.github.starnowski.jamolingo.junit5.MongoCollectionKey` ([#1](https://github.com/starnowski/jamolingo/issues/1))
*   `com.github.starnowski.jamolingo.junit5.MongoDocument` ([#1](https://github.com/starnowski/jamolingo/issues/1))
*   `com.github.starnowski.jamolingo.junit5.MongoSetup` ([#1](https://github.com/starnowski/jamolingo/issues/1))
*   `com.github.starnowski.jamolingo.junit5.QuarkusMongoDataLoaderExtension` ([#1](https://github.com/starnowski/jamolingo/issues/1))
