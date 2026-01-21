# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

* [Unreleased](#unreleased)
* [0.2.0](#020---2026-01-20)
* [0.1.0](#010---2026-01-18)

## [Unreleased]

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
