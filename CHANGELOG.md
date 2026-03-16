# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

* [Unreleased](#unreleased)
* [0.8.0](#080---2026-03-16)
* [0.7.0](#070---2026-03-06)
* [0.6.0](#060---2026-03-04)
* [0.5.1](#051---2026-03-03)
* [0.5.0](#050---2026-03-01)
* [0.4.1](#041---2026-01-26)
* [0.4.0](#040---2026-01-24)
* [0.3.0](#030---2026-01-21)
* [0.2.0](#020---2026-01-20)
* [0.1.0](#010---2026-01-18)

## [Unreleased]

## [0.8.0] - 2026-03-16

### Added

#### Core Module
*   Added support for `$search` operator translation to MongoDB Atlas Search aggregation stages. ([#37](https://github.com/starnowski/jamolingo/issues/37))
*   `com.github.starnowski.jamolingo.core.operators.search.ODataSearchToMongoAtlasSearchParser` class for parsing OData search options. ([#37](https://github.com/starnowski/jamolingo/issues/37))
*   `com.github.starnowski.jamolingo.core.operators.search.ODataSearchToMongoAtlasSearchOptions` and `DefaultODataSearchToMongoAtlasSearchOptions` for search configuration. ([#37](https://github.com/starnowski/jamolingo/issues/37))
*   Support for search score filtering using `$set` and `$match` stages with configurable field names. ([#37](https://github.com/starnowski/jamolingo/issues/37))
*   `com.github.starnowski.jamolingo.core.operators.search.SearchOperatorResult` and `SearchOperatorResultForAtlasSearch` interfaces. ([#37](https://github.com/starnowski/jamolingo/issues/37))

### Changed

#### Administrative
*   Updated project version to `0.8.0-SNAPSHOT`.

## [0.7.0] - 2026-03-06

### Added

#### Core Module
*   `/query-index-check` endpoint in demo applications (`spring-boot-webapp` and `quarkus-webapp`) for validating index usage in OData queries. ([#31](https://github.com/starnowski/jamolingo/issues/31))

#### JUnit 5 Mongo Extension Module
*   `junit5-mongo-extension-parent` module to manage specialized JUnit 5 extensions. ([#31](https://github.com/starnowski/jamolingo/issues/31))
*   `com.github.starnowski.jamolingo.junit5.AbstractMongoDataLoaderExtension` base class for JUnit 5 data loader extensions. ([#31](https://github.com/starnowski/jamolingo/issues/31))
*   `junit5-mongo-extension-spring` module providing `com.github.starnowski.jamolingo.junit5.SpringMongoDataLoaderExtension` for Spring and Spring Boot applications. ([#31](https://github.com/starnowski/jamolingo/issues/31))

#### Demos Module
*   `demos` module containing `spring-boot-webapp` and `quarkus-webapp` demonstration applications. ([#31](https://github.com/starnowski/jamolingo/issues/31))

#### Administrative
*   Changed project license from CC0 1.0 Universal to Apache License 2.0. ([#31](https://github.com/starnowski/jamolingo/issues/31))

### Changed

#### Core Module
*   Refactored `com.github.starnowski.jamolingo.core.operators.filter.MongoFilterVisitor` to make regex matching for `contains`, `startswith`, and `endswith` functions case-sensitive by default, aligning with the OData specification. ([#31](https://github.com/starnowski/jamolingo/issues/31))

#### JUnit 5 Mongo Extension Module
*   Renamed `junit5-mongo-extension` module to `junit5-mongo-extension-quarkus` and moved it under the `junit5-mongo-extension-parent` multi-module structure. ([#31](https://github.com/starnowski/jamolingo/issues/31))
*   Refactored `com.github.starnowski.jamolingo.junit5.QuarkusMongoDataLoaderExtension` to extend the new `AbstractMongoDataLoaderExtension` base class. ([#31](https://github.com/starnowski/jamolingo/issues/31))

## [0.6.0] - 2026-03-04

### Added

#### Core Module
*   `com.github.starnowski.jamolingo.core.operators.filter.LiteralToBsonConverter` interface for custom OData literal to BSON conversion. ([#24](https://github.com/starnowski/jamolingo/issues/24))
*   `com.github.starnowski.jamolingo.core.operators.filter.MongoFilterVisitorCommonContext` interface to provide shared context to the filter visitor. ([#24](https://github.com/starnowski/jamolingo/issues/24))
*   `com.github.starnowski.jamolingo.core.operators.filter.DefaultODataToBsonConverter` class providing default BSON conversion logic. ([#24](https://github.com/starnowski/jamolingo/issues/24))
*   `com.github.starnowski.jamolingo.core.operators.filter.DefaultLiteralToBsonConverter` class providing default literal conversion logic. ([#24](https://github.com/starnowski/jamolingo/issues/24))
*   `com.github.starnowski.jamolingo.core.operators.filter.DefaultMongoFilterVisitorCommonContext` class with builder support for easy context configuration. ([#24](https://github.com/starnowski/jamolingo/issues/24))

### Changed

#### Core Module
*   `com.github.starnowski.jamolingo.core.operators.filter.ODataToBsonConverter` converted from class to interface with updated `toBsonValue` method signature. ([#24](https://github.com/starnowski/jamolingo/issues/24))
*   Modified `com.github.starnowski.jamolingo.core.operators.filter.ODataFilterToMongoMatchParser` to remove `org.apache.olingo.commons.api.edm.Edm` parameter from all `parse` methods and added support for `MongoFilterVisitorCommonContext`. ([#24](https://github.com/starnowski/jamolingo/issues/24))
*   Refactored `com.github.starnowski.jamolingo.core.operators.filter.MongoFilterVisitor` to use `MongoFilterVisitorCommonContext` instead of direct `Edm` dependency and hardcoded conversion logic. ([#24](https://github.com/starnowski/jamolingo/issues/24))
*   Updated project URL in `pom.xml` to `https://github.com/starnowski/jamolingo`. ([#28](https://github.com/starnowski/jamolingo/issues/28))

## [0.5.1] - 2026-03-03

### Added

#### Core Module
*   `com.github.starnowski.jamolingo.core.operators.count.CountOperatorResult` ([#28](https://github.com/starnowski/jamolingo/issues/28))
*   `com.github.starnowski.jamolingo.core.operators.count.OdataCountToMongoCountParser` ([#28](https://github.com/starnowski/jamolingo/issues/28))

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
