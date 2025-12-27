# 📘 OData v4.x — One-Page Cheat Sheet

## 1. Path & Navigation

| Symbol | Meaning                                          |
| ------ | ------------------------------------------------ |
| `/`    | Path segment separator                           |
| `()`   | Entity key lookup / function & action parameters |
| `,`    | Composite key or parameter separator             |
| `:`    | Type cast or lambda variable                     |

**Examples**

```
Orders(1)/Customer/Address/City
Orders(ID=1,Type='A')
NS.VipCustomer/DiscountRate
```

---

## 2. System Query Options

| Option     | Purpose                       |
| ---------- | ----------------------------- |
| `$filter`  | Filter results                |
| `$select`  | Choose properties             |
| `$expand`  | Include related entities      |
| `$orderby` | Sort results                  |
| `$top`     | Limit results                 |
| `$skip`    | Offset results                |
| `$count`   | Include total count           |
| `$compute` | Calculated properties         |
| `$apply`   | Aggregation & transformations |

**Example**

```
$select=Name,Price&$filter=Price gt 100
```

---

## 3. Operators

### Comparison

```
eq  ne  gt  ge  lt  le
```

### Logical

```
and  or  not
```

### Arithmetic

```
add  sub  mul  div  mod
```

**Example**

```
$filter=Price gt 50 and Stock ge 10
```

---

## 4. Property Access (IMPORTANT)

✔ Use `/`
✘ Never use `.`

```
ShippingInfo/Address/City   ✔
ShippingInfo.Address.City   ✘
```

---

## 5. Lambda Expressions (Collections)

```
any(var: condition)
all(var: condition)
```

**Examples**

```
Orders/any(o: o/Total gt 1000)
Tags/all(t: t ne 'obsolete')
```

---

## 6. Annotations & Metadata

| Symbol | Meaning                  |
| ------ | ------------------------ |
| `@`    | Annotation marker        |
| `.`    | Namespace separator ONLY |

**Examples**

```
Name@odata.type
@odata.count
Edm.String
```

⚠️ `.` is **never** used for navigation.

---

## 7. Literals

| Type          | Syntax          |
| ------------- | --------------- |
| String        | `'text'`        |
| Escaped quote | `''`            |
| Boolean       | `true`, `false` |
| Null          | `null`          |
| Number        | `-42`           |

**Example**

```
$filter=Name eq 'O''Reilly'
```

---

## 8. Grouping & Precedence

| Symbol | Purpose           |
| ------ | ----------------- |
| `()`   | Group expressions |

```
$filter=(Price gt 10) and (Price lt 100)
```

---

## 9. Collection Indexing (OData v4.01+)

```
/0   Index into collection
```

```
Tags/0
```

---

## 10. URL Encoding (Common)

| Character | Encode As |
| --------- | --------- |
| Space     | `%20`     |
| `'`       | `%27`     |
| `+`       | `%2B`     |
| `#`       | `%23`     |
| `%`       | `%25`     |

---

## 🚫 Common Mistakes

* Using `.` instead of `/`
* Using `==` instead of `eq`
* Forgetting URL encoding
* Mixing SQL or JSONPath syntax

---

## 🧠 Quick Mental Model

```
/   → navigation
$   → system query
@   → annotation
.   → namespace
:   → lambda / type cast
()  → keys / grouping
```

---

