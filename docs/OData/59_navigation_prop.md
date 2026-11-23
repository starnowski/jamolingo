# Issue #59 Analysis: Navigation Properties in OData System Query Parameters and `$apply` Transformations

## 1. Executive Summary

This document analyzes which OData system query parameters and `$apply` set transformations can reference **navigation properties** according to:
1. **OData v4.0 / v4.01 Specification** (Part 2: URL Conventions) and **OData Extension for Data Aggregation v4.0**.
2. **Apache Olingo 5.0.0 URI Parser** (`odata-server-core:5.0.0`), which is used by `jamolingo` to parse OData queries into AST objects.
3. **MongoDB Aggregation Pipeline Implications** in `jamolingo` (specifically whether a `$lookup` and subsequent `$unset` stage are required).

### Quick Summary Matrix — System Query Parameters

| System Query Parameter | Allowed by OData Spec? | Supported by Olingo 5.0.0 Parser? | Requires MongoDB `$lookup` in `jamolingo`? | Notes |
| :--- | :---: | :---: | :---: | :--- |
| **`$filter`** | **YES** | **YES** | **YES** | Supports single-valued navigation paths, multi-hop paths, complex-to-navigation paths, `null` checks, type casts, and collection navigation via `any()`, `all()`, `/$count`, and key predicates. |
| **`$orderby`** | **YES** | **YES** | **YES** | **Confirmed.** Supports single-valued navigation paths (e.g. `Category/Name asc`), multi-hop paths, and collection navigation expressions that evaluate to a primitive value (e.g. `Items/$count desc`, `Items/any(...) desc`). |
| **`$select`** | **PARTIAL (Link only in v4.0; path in v4.01)** | **TERMINAL ONLY (`$select=NavProp`)** | **NO** | **Confirmed.** In OData v4.0 and Olingo 5.0.0, `$select=NavProp` (or `$select=ComplexProp/NavProp`) is valid and selects the navigation link, whereas `$select=NavProp/SubProp` is **rejected** by Olingo 5.0.0 (`$expand=NavProp($select=SubProp)` must be used instead). `$select` alone never triggers `$lookup`, though `$project` must not fail when `UriResourceNavigation` is present and must preserve expanded navigation properties. |
| **`$expand`** | **YES** | **YES** | **YES** | Designed specifically to expand navigation properties (including `ComplexProp/NavProp`), and its nested options (`$filter`, `$orderby`, `$apply`, `$expand`, `$select`) can further reference navigation properties of the target entity type. |
| **`$apply`** | **YES** | **YES** | **YES** | Depends on the specific transformation inside `$apply` (see the `$apply` matrix below). |
| **`$compute`** *(OData 4.01)* | **YES** *(v4.01)* | **NO** *(only inside `$apply`)* | **N/A** | Standalone `$compute` is an OData 4.01 addition not present in Olingo 5.0.0 `SystemQueryOptionKind`. Olingo supports `compute(...)` inside `$apply`. |
| **`$search`** | **NO** | **NO** | **NO** | Free-text search terms (`Word`, `Phrase`, `AND`, `OR`, `NOT`). Does not reference EDM property paths (though `$search` can be nested *inside* `$expand=NavProp($search=...)`). |
| **`$count`** | **NO** | **NO** | **NO** | Takes only boolean `true` or `false` (`?$count=true`). *(Note: `/$count` path segment on a collection navigation property is used inside `$filter`, `$orderby`, and `$apply`, not inside the `$count` query option).* |
| **`$top`** | **NO** | **NO** | **NO** | Takes only a non-negative integer literal. |
| **`$skip`** | **NO** | **NO** | **NO** | Takes only a non-negative integer literal. |
| **`$levels`** | **NO** | **NO** | **NO** | Allowed only inside `$expand=NavProp($levels=...)`; takes a non-negative integer or `max`. |
| **`$format`, `$id`, `$skiptoken`, `$deltatoken`** | **NO** | **NO** | **NO** | Administrative, addressing, format, or paging token parameters; cannot reference EDM properties. |

---

### Quick Summary Matrix — `$apply` Transformations (Operators)

| `$apply` Transformation | Olingo `ApplyItem.Kind` | Can Use Navigation Properties? | Supported by Olingo 5.0.0 `ApplyParser`? | Requires MongoDB `$lookup`? |
| :--- | :--- | :---: | :---: | :---: |
| **`filter(...)`** | `FILTER` | **YES** | **YES** | **YES** |
| **`groupby(...)`** | `GROUP_BY` | **YES** | **YES** | **YES** |
| **`aggregate(...)`** | `AGGREGATE` | **YES** | **YES** | **YES** |
| **`compute(...)`** | `COMPUTE` | **YES** | **YES** | **YES** |
| **`expand(...)`** | `EXPAND` | **YES** | **YES** | **YES** *(retained in output)* |
| **`orderby(...)`** | `ORDERBY` | **YES** | **YES** | **YES** |
| **`topcount(...)` / `bottomcount(...)`** | `BOTTOM_TOP` | **YES** | **YES** | **YES** |
| **`topsum(...)` / `bottomsum(...)`** | `BOTTOM_TOP` | **YES** | **YES** | **YES** |
| **`toppercent(...)` / `bottompercent(...)`** | `BOTTOM_TOP` | **YES** | **YES** | **YES** |
| **`concat(...)`** | `CONCAT` | **YES (via inner sequences)** | **YES** | **YES (in sub-pipelines)** |
| **`CustomFunction`** | `CUSTOM_FUNCTION` | **POTENTIALLY (via alias params)** | **YES** | **Depends on function** |
| **`search(...)`** | `SEARCH` | **NO** | **YES** | **NO** |
| **`top(...)`** | `TOP` | **NO** | **YES** | **NO** |
| **`skip(...)`** | `SKIP` | **NO** | **YES** | **NO** |
| **`identity`** | `IDENTITY` | **NO** | **YES** | **NO** |
| **`join(...)` / `outerjoin(...)`** | *(Not in Olingo 5.0.0)* | **YES (by OData spec)** | **NO** | **YES** |
| **`ancestors` / `descendants` / `traverse`** | *(Not in Olingo 5.0.0)* | **YES (by OData spec)** | **NO** | **YES** |

---

## 2. Detailed Analysis of System Query Parameters

### 2.1. `$filter` — **YES (Confirmed)**

According to OData v4.0/v4.01 Part 2 (Section 5.1.1) and Apache Olingo's `FilterParser` / `ExpressionParser`, `$filter` expressions can reference navigation properties in the following ways:

1. **Single-Valued Navigation Properties (`0..1` or `1`, `isCollection() == false`)**:
   - **Direct property access**:
     - `$filter=Category/Name eq 'Electronics'`
     - In Olingo AST: `Member` expression whose `UriInfoResource.getUriResourceParts()` contains `[UriResourceNavigationPropertyImpl("Category"), UriResourcePrimitivePropertyImpl("Name")]`.
   - **Multi-hop navigation paths**:
     - `$filter=Order/Customer/Address/City eq 'Warsaw'`
     - Contains multiple `UriResourceNavigation` segments followed by complex/primitive segments.
   - **Navigation property nested inside a Complex Property**:
     - `$filter=ShippingDetails/Carrier/Name eq 'DHL'`
     - Contains `[UriResourceComplexPropertyImpl("ShippingDetails"), UriResourceNavigationPropertyImpl("Carrier"), UriResourcePrimitivePropertyImpl("Name")]`.
   - **Entity `null` comparisons**:
     - `$filter=Category eq null` or `$filter=Category ne null`
     - Valid in both OData and Olingo 5.0.0 (`ExpressionParser.checkEqualityTypes` permits comparing a `Member` of `EdmEntityType` against the `null` literal).
   - **Type casts and `isof` / `cast` functions**:
     - `$filter=Category/MySchema.PremiumCategory/Tier gt 2`
     - `$filter=isof(Category, MySchema.PremiumCategory)`

2. **Collection-Valued Navigation Properties (`*`, `isCollection() == true`)**:
   - Directly accessing a structural property on a collection navigation property (e.g., `$filter=Items/Price gt 10`) is **forbidden** in OData and rejected by Olingo (`UriParserSemanticException: Collection not allowed`).
   - Instead, collection-valued navigation properties are accessed via:
     - **Lambda Operators (`any` and `all`)**:
       - Parameterless `any()`: `$filter=Items/any()` (evaluates to `true` if the related entity collection is non-empty).
       - Predicate `any(var: ...)`: `$filter=Items/any(i: i/Price gt 100)`
       - Predicate `all(var: ...)`: `$filter=Items/all(i: i/InStock eq true)`
       - Multi-hop / nested navigation inside lambda predicates:
         - `$filter=Orders/any(o: o/Supplier/Country eq 'PL')`
         - `$filter=Orders/any(o: o/Lines/any(l: l/Product/Price gt 50))`
     - **Collection Count (`/$count`)**:
       - `$filter=Items/$count gt 5`
       - `$filter=Items/MySchema.SpecialItem/$count eq 0`
       - In Olingo AST: `Member` expression with `[UriResourceNavigationPropertyImpl("Items"), UriResourceCountImpl]`.
     - **Key Predicate Lookup on a Collection Navigation Property**:
       - `$filter=Items(42)/Price gt 100`
       - In Olingo (`ExpressionParser.parseCollectionNavigationExpr`), key predicates on `UriResourceNavigationPropertyImpl` narrow the collection resource to a single entity, allowing further single-navigation path traversal (`/Price`).

---

### 2.2. `$orderby` — **YES (Confirmed)**

Issue #59 explicitly marks `$orderby (confirm)`. **Confirmation: YES, `$orderby` supports navigation properties.**

According to OData v4.0/v4.01 Part 2 (Section 5.1.2) and Apache Olingo's `OrderByParser` (which delegates each `OrderByItem` expression to `ExpressionParser`):

1. **Single-Valued Navigation Properties (`0..1` or `1`)**:
   - **Primitive property of a related entity**:
     - `$orderby=Category/Name asc`
   - **Multi-hop navigation and complex paths**:
     - `$orderby=Order/Customer/Address/City desc, Name asc`
     - `$orderby=Audit/CreatedBy/LastName asc`
   - **Scalar expressions / functions over navigation properties**:
     - `$orderby=tolower(Category/Name) asc`
     - `$orderby=Product/Price mul Quantity desc`

2. **Collection-Valued Navigation Properties (`*`)**:
   - A collection navigation property or its member properties cannot be used directly as a sort key (`$orderby=Items/Price` is rejected by Olingo with `COLLECTION_NOT_ALLOWED`).
   - However, any expression over a collection navigation property that evaluates to a **single primitive value** is valid:
     - **Sorting by related collection count (`/$count`)**:
       - `$orderby=Items/$count desc`
     - **Sorting by lambda boolean result (`any` / `all`)**:
       - `$orderby=Items/any(i: i/IsUrgent eq true) desc`
     - **Sorting by key-lookup on a collection navigation property**:
       - `$orderby=Items(1)/Price asc`

---

### 2.3. `$select` — **CONFIRMED: Terminal Navigation Link Only (No `$lookup` Required)**

Issue #59 explicitly marks `$select (TODO confrim)`. Here is the exact analysis of how OData and Apache Olingo 5.0.0 handle navigation properties in `$select`:

1. **OData v4.0 Specification (Part 2, Section 5.1.3)**:
   - A `selectItem` can be a structural property, a complex property path (`Address/City`), or a **navigation property** (`Category` or `ComplexProp/Category`).
   - **Crucial Semantic Rule**: In OData v4.0, specifying a navigation property in `$select` (e.g., `?$select=Name,Category`) **only selects the navigation link** (or deferred association metadata). It does **not** expand or fetch the related entity's fields unless `$expand=Category` is also present.
   - Furthermore, in OData v4.0, traversing *through* a navigation property inside `$select` (e.g., `?$select=Category/Name`) is **not allowed by the v4.0 grammar**. Instead, selecting fields of a related entity must be written using `$expand`:
     - `?$expand=Category($select=Name)`
   - *(Note: OData v4.01 later introduced `$select=Category/Name` as a shorthand, but Apache Olingo 5.0.0 implements the OData v4.0 `SelectParser` grammar).*

2. **Apache Olingo 5.0.0 `SelectParser` Behavior**:
   - In `org.apache.olingo.server.core.uri.parser.SelectParser.addSelectPath(...)`:
     - If a segment is an `EdmComplexType` property, `SelectParser` checks for `TokenKind.SLASH` and recursively parses nested structural or navigation properties (`ComplexProp/NavProp`).
     - If a segment is an `EdmNavigationProperty`, `SelectParser` adds `UriResourceNavigationPropertyImpl` to the `SelectItem`'s resource path and **immediately stops parsing further `/` segments**.
     - Consequently:
       - `$select=Category` -> **Valid** in Olingo 5.0.0 (produces `[UriResourceNavigationPropertyImpl("Category")]`).
       - `$select=ComplexProp/Category` -> **Valid** in Olingo 5.0.0 (produces `[UriResourceComplexPropertyImpl("ComplexProp"), UriResourceNavigationPropertyImpl("Category")]`).
       - `$select=Category/Name` -> **Rejected** by Olingo 5.0.0 with `UriParserSyntaxException: Illegal value of '$select' option!`.

3. **Implications for `jamolingo` (`OdataSelectToMongoProjectParser`)**:
   - **Does `$select` require generating a `$lookup` stage?**
     - **No.** `$select` alone never joins foreign collections; joining related documents is strictly the responsibility of `$expand` (or `$apply=expand(...)`).
   - **What needs to be handled in `OdataSelectToMongoProjectParser`?**
     - Currently, `OdataSelectToMongoProjectParser.java` (lines 81–84) checks:
       ```java
       if (!item.getResourcePath().getUriResourceParts().stream()
           .allMatch(p -> p instanceof UriResourceProperty)) {
         throw new RuntimeException("Invalid select parameter " + item.getResourcePath());
       }
       ```
       Because `UriResourceNavigation` does **not** extend `UriResourceProperty` (`UriResourceProperty` only covers primitive and complex properties), passing `$select=Category` currently throws `RuntimeException("Invalid select parameter")`.
     - Additionally, per OData v4.0 Section 5.1.3: if a query combines `$expand=Category` and `$select=Name` (or `$select=Name,Category`), the `$project` stage generated for `$select` must **not** strip out the expanded `Category` field produced by `$expand`.

---

### 2.4. `$expand` — **YES (Primary Mechanism for Navigation Properties)**

In OData v4.0/v4.01 (Section 5.1.2) and Olingo's `ExpandParser`:
1. **Expand Path (`ExpandParser.parseExpandPath`)**:
   - Can be a direct navigation property (`$expand=Category`), a navigation property reached via one or more complex properties (`$expand=ComplexProp/Category`), or a star expansion (`$expand=*`, `$expand=*/$ref`).
   - Can include type filters (`$expand=MySchema.SpecialEntity/Category` or `$expand=Category/MySchema.SpecialCategory`).
   - Can end with `/$ref` (`$expand=Category/$ref`) or `/$count` (`$expand=Items/$count`).
2. **Nested System Query Options inside `$expand`**:
   - For `$expand=NavProp(...)`: supports nested `$filter`, `$orderby`, `$select`, `$expand`, `$apply`, `$search`, `$top`, `$skip`, `$count`, and `$levels`.
   - For `$expand=NavProp/$count(...)`: supports nested `$filter` and `$search`.
   - For `$expand=NavProp/$ref(...)`: supports nested `$filter`, `$orderby`, `$search`, `$skip`, `$top`, and `$count`.
   - **Crucially**, any nested `$filter`, `$orderby`, or `$apply` inside `$expand=NavProp(...)` is evaluated in the context of `NavProp`'s `EdmEntityType` and **can itself reference navigation properties belonging to `NavProp`** (e.g., `$expand=Orders($filter=Customer/Country eq 'PL';$orderby=Shipper/Name asc)`).

---

## 3. Detailed Analysis of `$apply` System Query Parameter and Its Operators

The `$apply` system query option (defined in the **OData Extension for Data Aggregation Version 4.0**) executes a sequence of set transformations separated by `/`. Each transformation can inspect or reshape the entity stream.

Below is the analysis of every operator/transformation within `$apply`.

---

### 3.1. `filter(...)` (`ApplyItem.Kind.FILTER`) — **YES**

- **Syntax**: `$apply=filter(booleanExpr)`
- **OData Spec & Olingo 5.0.0**:
  - `ApplyParser` delegates `filter(...)` directly to `FilterParser` / `ExpressionParser`.
  - All navigation property usages supported in top-level `$filter` are equally supported inside `$apply=filter(...)`:
    - Single-valued navigation paths: `$apply=filter(Category/Name eq 'Books')`
    - Multi-hop paths: `$apply=filter(Order/Customer/Address/Country eq 'PL')`
    - Complex-to-navigation paths: `$apply=filter(Details/Vendor/Active eq true)`
    - Collection-valued navigation with `any` / `all`: `$apply=filter(Items/any(i: i/Price gt 100))`
    - Collection-valued navigation with `/$count`: `$apply=filter(Items/$count ge 1)`
    - Key predicates: `$apply=filter(Items(1)/Price gt 100)`
- **Pipeline Impact**:
  - Requires `$lookup` for the referenced navigation properties prior to the `$match` stage.
  - Because `filter(...)` does not redefine the document shape (`isDocumentShapeRedefined() == false`), the looked-up navigation property must be removed via `$unset` at the end of the pipeline unless it was also expanded via `expand(...)` or `$expand`.

---

### 3.2. `groupby(...)` (`ApplyItem.Kind.GROUP_BY`) — **YES**

- **Syntax**: `$apply=groupby((groupingElement1, groupingElement2, ...)[, subTransformationSequence])`
- **OData Spec (Section 3.10) & Olingo 5.0.0 (`ApplyParser.parseGroupByTrafo`)**:
  Navigation properties can be used in **two** places within `groupby`:

  1. **Inside the Grouping Property List `( ... )`**:
     - Olingo's `parseGroupingProperty` calls `parsePathPrefix`, which resolves chains of `EdmNavigationProperty`, `EdmComplexProperty`, and `EdmPrimitiveProperty` (including optional type casts):
       - **Single-valued navigation primitive property**:
         - `$apply=groupby((Category/Name))`
         - `$apply=groupby((Customer/Address/Country, Product/Category/Name))`
       - **Collection-valued navigation primitive property**:
         - `$apply=groupby((Orders/Status))`
         - *(Per OData Aggregation Section 3.10, grouping by a property of a collection-valued navigation property groups over the flattened/unwound related entities).*
       - **Terminal navigation property**:
         - `$apply=groupby((Category))`
         - Olingo's `parsePathPrefix` (lines 567–573) allows the path to end directly on an `EdmNavigationProperty` (`UriResourceNavigationPropertyImpl`).
       - **Inside `$rollup(...)`**:
         - `$apply=groupby(($rollup(Customer/Country, Customer/City)))`
         - `$apply=groupby(($rollup($all, Category/Department, Category/SubDepartment)))`
         - Olingo's `parseRollUpSpec` calls `parseGroupingProperty` for each rollup item, fully supporting navigation paths.

  2. **Inside the Second Argument (Sub-Transformation Sequence)**:
     - E.g., `$apply=groupby((Customer/Country), aggregate(Orders/Amount with sum as TotalSales))`
     - E.g., `$apply=groupby((CategoryId), aggregate(Products(Price with average as AvgPrice)))`
     - Any transformation chained inside `groupby` (`aggregate`, `filter`, `compute`, etc.) can reference navigation properties of the input entity type.

- **Pipeline Impact**:
  - Requires `$lookup` (and `$unwind` when grouping by a property inside a looked-up array) **before** the `$group` stage.
  - Because `groupby` redefines the document shape (`isDocumentShapeRedefined() == true`) and projects only the grouped paths and aggregated aliases (`_id: 0`), non-grouped root properties are automatically stripped by `$group` + `$project`. However, if a navigation property path like `Category/Name` is in the grouping key, OData expects the output structure to nest `Name` under `Category`: `{ "Category": { "Name": "..." } }`.

---

### 3.3. `aggregate(...)` (`ApplyItem.Kind.AGGREGATE`) — **YES**

- **Syntax**: `$apply=aggregate(aggregateExpr1, aggregateExpr2, ...)`
- **OData Spec (Section 3.1) & Olingo 5.0.0 (`ApplyParser.parseAggregateExpr`)**:
  Olingo 5.0.0 supports navigation properties in `aggregate(...)` across **six distinct syntactic patterns**:

  1. **Single-Valued Navigation Property Path with Standard/Custom Method**:
     - `$apply=aggregate(Category/Discount with average as AvgDiscount)`
     - `$apply=aggregate(Order/Customer/Score with max as MaxCustomerScore)`
     - Parsed by Olingo via `ExpressionParser` into `AggregateExpression.getExpression()` as a `Member` containing `UriResourceNavigation` segments.

  2. **Arithmetic / Scalar Expression across Single-Valued Navigation Properties**:
     - `$apply=aggregate(Product/Price mul Quantity with sum as TotalRevenue)`
     - Parsed by Olingo into `AggregateExpression.getExpression()` as a `Binary` expression containing `Member` operands with `UriResourceNavigation`.

  3. **Inline Aggregation over a Navigation Property (`NavProp(inlineAggregateExpr)`)**:
     - **Essential for Collection-Valued Navigation Properties!**
     - Because `ExpressionParser` forbids `CollectionNavProp/PrimitiveProp` in a flat `Member` expression, OData Aggregation (Section 3.1.2.2.2) and Olingo (`ApplyParser.java` lines 262–273) support parenthesized inline aggregate expressions:
       - `$apply=aggregate(Orders(Amount with sum as TotalOrderAmount))`
       - Multi-hop inline aggregation:
         `$apply=aggregate(Orders(Lines(Quantity with sum as TotalQuantity)))`
       - Complex + navigation prefix:
         `$apply=aggregate(SalesInfo/Orders(Amount with max as MaxOrderAmount))`
     - In Olingo AST:
       - `expr.getPath()` contains the prefix `UriInfo` (`[UriResourceNavigationPropertyImpl("Orders")]`).
       - `expr.getInlineAggregateExpression()` contains the nested `AggregateExpression` (`Amount with sum as TotalOrderAmount`).

  4. **Path Prefix + `$count` on a Collection-Valued Navigation Property**:
     - `$apply=aggregate(Orders/$count as TotalOrdersCount)`
     - In Olingo AST (`ApplyParser.java` lines 274–281):
       - `expr.getPath()` contains `[UriResourceNavigationPropertyImpl("Orders"), UriResourceCountImpl]`.
       - `expr.getAlias()` is `"TotalOrdersCount"`, and `expr.getStandardMethod()` is `null`.

  5. **Expression `/$count` with Aggregation Method (`with sum`, `with average`, etc.)**:
     - `$apply=aggregate(Orders/$count with average as AvgOrdersPerCustomer)`
     - `$apply=aggregate(Orders/$count with sum as TotalOrders)`
     - In Olingo AST: `ExpressionParser` parses `Orders/$count` as a `Member` expression of type `Edm.Int64` (`expr.getExpression()`), with `expr.getStandardMethod() == StandardMethod.AVERAGE` (or `SUM`, `MIN`, `MAX`, `COUNT_DISTINCT`).

  6. **`from` Clause inside `aggregate(...)` (`parseAggregateFrom`)**:
     - `$apply=aggregate(Amount with sum as Total from Customer/Country with average)`
     - In Olingo AST (`ApplyParser.java` lines 402–410): `expr.getFrom()` contains `AggregateExpression` items whose `getExpression()` is a `Member` parsed via `parseGroupingProperty` (which supports navigation paths).

- **Pipeline Impact**:
  - Requires `$lookup` before `$group`.
  - Because `aggregate` redefines the document shape (`isDocumentShapeRedefined() == true`) into a single summary document (or per-group summary inside `groupby`) containing only the aggregated aliases, no `$unset` stage is needed after `aggregate`.

---

### 3.4. `compute(...)` (`ApplyItem.Kind.COMPUTE`) — **YES**

- **Syntax**: `$apply=compute(primitiveExpr as alias, ...)`
- **OData Spec (Section 3.13) & Olingo 5.0.0 (`ApplyParser.parseComputeTrafo`)**:
  - `compute` evaluates any `ExpressionParser` expression that yields an `EdmPrimitiveType` and adds it as a dynamic property alias.
  - **Supported Navigation Property Usages**:
    1. **Single-valued navigation property expressions**:
       - `$apply=compute(Product/Price mul Quantity as TotalItemCost)`
       - `$apply=compute(concat(Customer/FirstName, Customer/LastName) as CustomerFullName)`
       - `$apply=compute(year(Order/CreatedAt) as OrderYear)`
    2. **Collection-valued navigation property `/$count`**:
       - `$apply=compute(Orders/$count as OrderCount)`
       - `$apply=compute(Orders/$count gt 0 as HasOrders)`
    3. **Collection-valued navigation property `any(...)` / `all(...)`** (evaluates to `Edm.Boolean`, which is a primitive type):
       - `$apply=compute(Orders/any(o: o/Status eq 'OVERDUE') as HasOverdueOrders)`
       - `$apply=compute(Orders/all(o: o/Paid eq true) as AllOrdersPaid)`
    4. **Key predicate lookup on collection-valued navigation property**:
       - `$apply=compute(Orders(1)/Total as FirstOrderTotal)`
    5. **`aggregate(...)` Method Call inside `compute` (`MethodKind.COMPUTE_AGGREGATE`)**:
       - In Olingo's `ExpressionParser.java` (lines 127 & 666–671), `aggregate(...)` is registered as an expression method (`MethodKind.COMPUTE_AGGREGATE`) that delegates to `ApplyParser.parseAggregateMethodCallExpr(...)` (with `Requirement.FORBIDDEN` for `as` alias inside the method call):
         - `$apply=compute(aggregate(Orders(Amount with sum)) as TotalOrderAmount)`
         - `$apply=compute(aggregate(Orders/$count) as OrderCount)`
- **Pipeline Impact**:
  - Requires `$lookup` prior to the `$addFields` / `$set` stage.
  - Because `compute` preserves the entity shape (`isDocumentShapeRedefined() == false`) and only appends computed aliases, any navigation property looked up solely for `compute` must be removed via `$unset` at the end of the pipeline (unless expanded).

---

### 3.5. `expand(...)` (`ApplyItem.Kind.EXPAND`) — **YES**

- **Syntax**:
  - `$apply=expand(NavProp)`
  - `$apply=expand(ComplexProp/NavProp)`
  - `$apply=expand(NavProp, filter(booleanExpr))`
  - `$apply=expand(NavProp, expand(SubNavProp))`
  - `$apply=expand(NavProp, filter(booleanExpr), expand(SubNavProp1), expand(SubNavProp2))`
- **OData Spec (Section 3.6) & Olingo 5.0.0 (`ApplyParser.parseExpandTrafo`)**:
  - `expand(...)` in `$apply` directly targets a navigation property (optionally preceded by complex properties and type casts).
  - It can contain an inner `filter(...)` transformation and/or one or more nested `expand(...)` transformations.
  - Any inner `filter(...)` inside `expand(NavProp, filter(...))` is parsed against `NavProp`'s entity type and can itself reference further navigation properties of `NavProp` (e.g., `$apply=expand(Orders, filter(Customer/Country eq 'PL'))`).
- **Pipeline Impact**:
  - Translates to `$lookup` stages (already supported in `jamolingo` via `ExpandItemParser` from Issue #58).
  - **Crucial Distinction**: Unlike implicit `$lookup` stages added to support `filter`, `orderby`, `compute`, or `bottomtop`, a navigation property explicitly expanded via `$apply=expand(NavProp)` **must NOT be removed by `$unset`** (unless a subsequent shape-redefining transformation like `groupby` or `aggregate` removes it).

---

### 3.6. `orderby(...)` (`ApplyItem.Kind.ORDERBY`) — **YES**

- **Syntax**: `$apply=orderby(expr1 [asc|desc], expr2 [asc|desc], ...)`
- **Olingo 5.0.0 (`ApplyParser.java` lines 184–188)**:
  - Olingo 5.0.0 supports `orderby(...)` as a set transformation inside `$apply`, delegating directly to `OrderByParser`.
  - Supports the exact same navigation property expressions as the top-level `$orderby` system query parameter:
    - Single-valued navigation paths: `$apply=orderby(Category/Name asc, Supplier/Address/City desc)`
    - Collection-valued navigation count: `$apply=orderby(Orders/$count desc)`
    - Collection-valued navigation lambda boolean expression: `$apply=orderby(Orders/any(o: o/Priority eq 1) desc)`
- **Pipeline Impact**:
  - Requires `$lookup` before the `$sort` stage, and `$unset` at the end if not expanded.

---

### 3.7. `topcount(...)`, `bottomcount(...)`, `topsum(...)`, `bottomsum(...)`, `toppercent(...)`, `bottompercent(...)` (`ApplyItem.Kind.BOTTOM_TOP`) — **YES**

- **Syntax**: `$apply=topcount(numberExpr, valueExpr)` *(and `bottomcount`, `topsum`, `bottomsum`, `toppercent`, `bottompercent`)*
- **OData Spec (Sections 3.3–3.5) & Olingo 5.0.0 (`ApplyParser.parseBottomTop`)**:
  - Olingo parses both parameters using `ExpressionParser`:
    - 1st parameter (`number`): checked with `checkIntegerType(number)` (typically an integer literal, though syntactically any non-collection integer expression).
    - 2nd parameter (`value`): checked with `checkNumericType(value)`.
  - **Supported Navigation Property Usages in the 2nd Parameter (`value`)**:
    1. **Single-valued navigation property numeric field**:
       - `$apply=topcount(5, Product/Price)`
       - `$apply=bottomsum(500, Order/Invoice/Amount)`
       - `$apply=toppercent(25, Category/PriorityWeight)`
    2. **Arithmetic expression using navigation properties**:
       - `$apply=topcount(10, Product/Price mul Quantity)`
    3. **Collection-valued navigation property `/$count`** (evaluates to `Edm.Int64`, which satisfies `checkNumericType`):
       - `$apply=topcount(5, Orders/$count)` (returns the top 5 entities with the highest number of related `Orders`).
    4. **Key predicate lookup on collection-valued navigation property**:
       - `$apply=topcount(5, Items(1)/Score)`
- **Pipeline Impact**:
  - Requires `$lookup` prior to the `$sort` / `$setWindowFields` stages generated by `BottomTopItemParser`.
  - Note: Currently `BottomTopItemParser` only inspects `member.getResourcePath().getUriResourceParts().get(0).getSegmentValue()`, so it will need to resolve the full `member.getResourcePath()` (and support expressions/navigation paths).
  - Because `BottomTop` preserves the entity shape (`isDocumentShapeRedefined() == false`), any navigation property looked up solely for `BottomTop` must be removed via `$unset` at the end if not expanded.

---

### 3.8. `concat(...)` (`ApplyItem.Kind.CONCAT`) — **YES (Via Sub-Transformation Sequences)**

- **Syntax**: `$apply=concat(applySequence1, applySequence2, ...)`
- **OData Spec (Section 3.8) & Olingo 5.0.0 (`ApplyParser.parseConcatTrafo`)**:
  - `concat` takes two or more full `ApplyOption` sequences, executes each on the input set, and concatenates the results.
  - Each inner sequence can contain `filter`, `groupby`, `aggregate`, `compute`, `orderby`, `expand`, `topcount`, etc., all of which **can use navigation properties**:
    - `$apply=concat(filter(Category/Name eq 'Books'), filter(Supplier/Country eq 'US'))`
    - `$apply=concat(groupby((Category/Name), aggregate($count as Cnt)), groupby((Supplier/Name), aggregate($count as Cnt)))`
- **Pipeline Impact**:
  - `ConcatItemParser` translates each branch into a sub-pipeline inside a `$facet` stage.
  - Navigation properties used within `concat` branches either need `$lookup` inside the respective `$facet` sub-pipeline (or before `$facet` if shared) and proper `$unset` handling for branches that do not redefine the document shape or expand the navigation property.

---

### 3.9. `CustomFunction` (`ApplyItem.Kind.CUSTOM_FUNCTION`) — **POTENTIALLY**

- **Syntax**: `$apply=MyNamespace.MyCustomTrafo(param1=..., param2=...)`
- **OData Spec (Section 3.12) & Olingo 5.0.0 (`ApplyParser.parseCustomFunction`)**:
  - Represents a bound OData function acting as a set transformation on a collection of entities or complex types.
  - Function parameters can use parameter aliases (`@p1`), and alias expressions are parsed via `ExpressionParser` in the context of the referring type, which can reference navigation properties.
  - *(Currently not implemented in `jamolingo`; marked as `// TODO CUSTOM_FUNCTION` in `ODataApplyToMongoAggregationPipelineParser`).*

---

### 3.10. `$apply` Transformations That Cannot Use Navigation Properties

1. **`search(...)` (`ApplyItem.Kind.SEARCH`)**:
   - Uses `SearchParser` (free-text words/phrases and boolean `AND`/`OR`/`NOT`). Cannot reference EDM property or navigation paths.
2. **`top(...)` (`ApplyItem.Kind.TOP`)**:
   - Takes only a non-negative integer literal (e.g., `top(10)`).
3. **`skip(...)` (`ApplyItem.Kind.SKIP`)**:
   - Takes only a non-negative integer literal (e.g., `skip(5)`).
4. **`identity` (`ApplyItem.Kind.IDENTITY`)**:
   - Takes no arguments and returns the input set unchanged.

---

### 3.11. OData Aggregation v4.0 Transformations Not Supported by Olingo 5.0.0 `ApplyParser`

For completeness, the OData Data Aggregation v4.0 specification defines two additional categories of transformations that inherently operate on navigation properties, but **Apache Olingo 5.0.0 does not have built-in parser tokens or AST classes for them**:

1. **`join(NavProp as Alias[, applySequence])` and `outerjoin(NavProp as Alias[, applySequence])` (Section 3.11)**:
   - Specifically designed to join source entities with related entities from a navigation property (`NavProp`) under an alias (`Alias`), optionally applying nested transformations to the related entities before joining.
   - Although `jamolingo` contains a stub `JoinItemParser` class, Olingo 5.0.0's `UriTokenizer` and `ApplyParser` do not recognize `join(` or `outerjoin(`.
2. **Hierarchical Transformations: `ancestors(...)`, `descendants(...)`, `traverse(...)` (Section 6.3)**:
   - Operate on self-referencing recursive navigation properties annotated with `Org.OData.Aggregation.V1.RecursiveHierarchy`.
   - As documented in `CHRONICLE.md`, Olingo 5.0.0 does not support these transformations in `UriTokenizer` or `ApplyParser`.

---

## 4. Summary of Navigation Property Path Patterns & Architectural Considerations for Issue #59

### 4.1. All Navigation Path Patterns to Consider in AST Resolution

When extracting used navigation properties from Olingo `UriInfoResource` / `Expression` objects across `$filter`, `$orderby`, and `$apply`, the following path structures can occur:

| Pattern | Example | Olingo `UriResource` Sequence |
| :--- | :--- | :--- |
| **Single-hop single-valued navigation** | `Category/Name` | `[UriResourceNavigation("Category"), UriResourcePrimitiveProperty("Name")]` |
| **Multi-hop navigation** | `Order/Customer/City` | `[UriResourceNavigation("Order"), UriResourceNavigation("Customer"), UriResourcePrimitiveProperty("City")]` |
| **Navigation inside Complex Property** | `Details/Manufacturer/Name` | `[UriResourceComplexProperty("Details"), UriResourceNavigation("Manufacturer"), UriResourcePrimitiveProperty("Name")]` |
| **Complex Property inside Navigation** | `Category/AuditInfo/CreatedBy` | `[UriResourceNavigation("Category"), UriResourceComplexProperty("AuditInfo"), UriResourcePrimitiveProperty("CreatedBy")]` |
| **Terminal Navigation Property** | `$filter=Category eq null` or `$apply=groupby((Category))` | `[UriResourceNavigation("Category")]` |
| **Collection Navigation + `/$count`** | `Orders/$count` | `[UriResourceNavigation("Orders"), UriResourceCount]` |
| **Collection Navigation + `any()` / `all()`** | `Orders/any(o: o/Total gt 100)` | Outer: `[UriResourceNavigation("Orders"), UriResourceLambdaAny("o", expr)]`<br>Inner `expr`: `[UriResourceLambdaVariable("o"), UriResourcePrimitiveProperty("Total")]` |
| **Nested Navigation inside Lambda** | `Orders/any(o: o/Customer/Country eq 'PL')` | Outer: `[UriResourceNavigation("Orders"), UriResourceLambdaAny("o", expr)]`<br>Inner `expr`: `[UriResourceLambdaVariable("o"), UriResourceNavigation("Customer"), UriResourcePrimitiveProperty("Country")]` |
| **Inline Aggregate on Collection Navigation** | `aggregate(Orders(Total with sum as S))` | Outer `expr.getPath()`: `[UriResourceNavigation("Orders")]`<br>Inner `expr.getInlineAggregateExpression()`: `[UriResourcePrimitiveProperty("Total")]` |

### 4.2. Key Rules for `$lookup` and `$unset` Generation

1. **Tracking Used vs. Expanded Navigation Properties**:
   - Every operator result (`OlingoOperatorResult`) should report the **navigation properties used** (e.g., `Category`, `Order/Customer`, `Details/Manufacturer`, `Orders`) separately from nested structural properties of the target entity (`Name`, `City`, `Total`).
2. **When to Generate `$lookup`**:
   - Before executing `$match` (`$filter` / `$apply=filter`), `$sort` (`$orderby` / `$apply=orderby`), `$group` (`$apply=groupby` / `$apply=aggregate`), `$addFields` (`$apply=compute`), or `$setWindowFields` (`$apply=topcount`/etc.), any navigation property referenced by that stage (that has not already been looked up in the current scope) must be joined using `$lookup`.
   - For single-valued navigation properties (`isCollection() == false`), remember that MongoDB's `$lookup` always produces an **array** field by default (though MongoDB dot-notation matching `{"Category.Name": "Books"}` and sorting `{"Category.Name": 1}` work across arrays, expressions in `$expr`, `$addFields`, or `$group` may require `$first` / `$arrayElemAt` or `$unwind` with `preserveNullAndEmptyArrays: true` so scalar operations like `Product/Price mul Quantity` receive a scalar value rather than a 1-element array).
3. **When to Generate `$unset`**:
   - If a navigation property was joined via `$lookup` to satisfy `$filter`, `$orderby`, or non-reshaping `$apply` transformations (`filter`, `compute`, `orderby`, `bottomtop`), and:
     1. The navigation property was **not** requested in `$expand` or `$apply=expand(...)`, AND
     2. The document shape was **not** redefined by `groupby` or `aggregate` (`isDocumentShapeRedefined() == false`),
   - Then an `$unset` stage must remove the temporary looked-up navigation property field before returning the final results.
