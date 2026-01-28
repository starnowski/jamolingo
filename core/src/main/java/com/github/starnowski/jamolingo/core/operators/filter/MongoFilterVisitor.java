package com.github.starnowski.jamolingo.core.operators.filter;

import com.google.common.collect.Streams;
import com.mongodb.client.model.Filters;
import lombok.Builder;
import org.apache.olingo.commons.api.edm.*;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.*;
import org.apache.olingo.server.api.uri.queryoption.expression.*;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class MongoFilterVisitor implements ExpressionVisitor<Bson> {

    public static final String CUSTOM_LITERAL_VALUE_PROPERTY = "$odata.literal";
    public static final String ODATA_MEMBER_PROPERTY = "$odata.member";
    public static final String ODATA_MEMBER_TYPE_PROPERTY = "$odata.member.type";
    private final Edm edm;
    private final MongoFilterVisitorContext context;


    public MongoFilterVisitor(Edm edm) {
        this(edm, MongoFilterVisitorContext.builder().build());
    }

    public MongoFilterVisitor(Edm edm, MongoFilterVisitorContext context) {
        this.edm = edm;
        this.context = context;
    }

    public static Document literal(Object value) {
        return new Document(CUSTOM_LITERAL_VALUE_PROPERTY, value);
    }

    private static String extractFieldName(Member member) {
        StringBuilder field = new StringBuilder();
        Iterator<UriResource> it = member.getResourcePath().getUriResourceParts().iterator();
        boolean nested = false;
        while (it.hasNext()) {
            UriResource property = it.next();
            if (property instanceof UriResourcePrimitiveProperty || property instanceof UriResourceComplexProperty) {
                UriResourceProperty uiProperty = (UriResourceProperty) property;
                if (nested) {
                    field.append(".");
                }
                field.append(uiProperty.getProperty().getName());
            } else {
                break;
            }
            nested = true;
        }
        return field.toString();
    }

    // --- Literals ---
    @Override
    public Bson visitLiteral(Literal literal) {
        String text = literal.getText();
        if ("null".equals(text)) {
            return literal(null);
        }
        if (text.startsWith("'") && text.endsWith("'")) {
            /*
             * Custom support of "normalize" method because there is a problem with adding custom method to Olingo project.
             */
            if (text.startsWith("'normalize('") && text.endsWith("')'")) {
                return literal(NormalizeHelper.normalize(text.substring("'normalize('".length() + 1, text.length() - "')'".length() - 1))); // placeholder, field comes later
            }
            return literal(text.substring(1, text.length() - 1)); // placeholder, field comes later
        }
        try {
            return literal(Integer.parseInt(text));
        } catch (NumberFormatException e) {
            try {
                return literal(Double.parseDouble(text));
            } catch (NumberFormatException ignored) {
            }
        }
        return literal(text);
    }

    // --- Members (fields) ---
    @Override
    public Bson visitMember(Member member) {
        if (member.getResourcePath().getUriResourceParts().size() == 1) {
            String field = extractFieldName(member);
            if (member.getResourcePath().getUriResourceParts().get(0) instanceof UriResourceLambdaVariable variable) {
                if ((this.context.isLambdaAnyContext() || this.context.isLambdaAllContext()) && this.context.lambdaVariableAliases().containsKey(variable.getVariableName())) {
                    if (this.context.isExprMode()) {
                        return prepareMemberDocument("$$" + variable.getVariableName(), variable.getType());
                    }
                    return this.context.lambdaVariableAliases().get(variable.getVariableName());
                }
                return prepareMemberDocument(field, variable.getType());
            } else {
                return prepareMemberDocument(field);
            }
        } else {
            UriResource last = member.getResourcePath().getUriResourceParts().get(member.getResourcePath().getUriResourceParts().size() - 1);
            if (last instanceof UriResourceLambdaAny any) {
                String field = extractFieldName(member);
                return getBsonForUriResourceLambdaAny(any, field);
            } else if (last instanceof UriResourceLambdaAll all) {
                String field = extractFieldName(member);
                return getBsonForUriResourceLambdaAll(all, field);
            }

        }
        String field = extractFieldName(member);
        return prepareMemberDocument(field);
    }

    private Bson getBsonForUriResourceLambdaAll(UriResourceLambdaAll all, String field) {
        Supplier<Bson> function = () -> {
            MongoFilterVisitor innerMongoFilterVisitor = new MongoFilterVisitor(edm,
                    MongoFilterVisitorContext.builder()
                            .lambdaVariableAliases(Map.of(all.getLambdaVariable(), prepareMemberDocument(field)))
                            .isLambdaAllContext(true)
                            .build());
            return innerMongoFilterVisitor.visitLambdaExpression("ALL", all.getLambdaVariable(), all.getExpression());
        };

        boolean expressionOperantRequiredExceptionThrown = false;
        boolean elementMatchOperantRequiredExceptionThrown = false;
        boolean multipleElementMatchOperantRequiredExceptionThrown = false;
        boolean allVariantTested = false;
        while (!allVariantTested) {
            try {
                allVariantTested = expressionOperantRequiredExceptionThrown && elementMatchOperantRequiredExceptionThrown && multipleElementMatchOperantRequiredExceptionThrown;
                return function.get();
            } catch (ExpressionOperantRequiredException ex) {
                expressionOperantRequiredExceptionThrown = true;
                function = () -> {
                    MongoFilterVisitor innerMongoFilterVisitor = new MongoFilterVisitor(edm,
                            MongoFilterVisitorContext.builder()
                                    .lambdaVariableAliases(Map.of(all.getLambdaVariable(), prepareMemberDocument(field)))
                                    .isLambdaAllContext(true)
                                    .isExprMode(true)
                                    .build());
                    Bson innerObject = innerMongoFilterVisitor.visitLambdaExpression("ALL", all.getLambdaVariable(), all.getExpression());
                    return prepareExprDocumentForAnyLambdaWithExpr(innerObject, field, all.getLambdaVariable());
                };
            } catch (ElementMatchOperantRequiredException ex) {
                elementMatchOperantRequiredExceptionThrown = true;
                function = () -> {
                    MongoFilterVisitor innerMongoFilterVisitor = new MongoFilterVisitor(edm,
                            MongoFilterVisitorContext.builder()
                                    .lambdaVariableAliases(Map.of(all.getLambdaVariable(), prepareMemberDocument(field)))
                                    .isLambdaAllContext(true)
                                    .elementMatchContext(new ElementMatchContext(field, false))
                                    .build());
                    Bson innerObject = innerMongoFilterVisitor.visitLambdaExpression("ALL", all.getLambdaVariable(), all.getExpression());
                    return prepareElementMatchDocumentForAllLambda(innerObject, field);
                };
            } catch (MultipleElementMatchOperantRequiredException ex) {
                multipleElementMatchOperantRequiredExceptionThrown = true;
                function = () -> {
                    MongoFilterVisitor innerMongoFilterVisitor = new MongoFilterVisitor(edm,
                            MongoFilterVisitorContext.builder()
                                    .lambdaVariableAliases(Map.of(all.getLambdaVariable(), prepareMemberDocument(field)))
                                    .isLambdaAllContext(true)
                                    .elementMatchContext(new ElementMatchContext(field, true))
                                    .build());
                    return innerMongoFilterVisitor.visitLambdaExpression("ALL", all.getLambdaVariable(), all.getExpression());
                };
            }
        }
        return null;
    }

    private Bson getBsonForUriResourceLambdaAny(UriResourceLambdaAny any, String field) {
        Supplier<Bson> function = () -> {
            MongoFilterVisitor innerMongoFilterVisitor = new MongoFilterVisitor(edm,
                    MongoFilterVisitorContext.builder()
                            .lambdaVariableAliases(Map.of(any.getLambdaVariable(), prepareMemberDocument(field)))
                            .isLambdaAnyContext(true)
                            .build());
            return innerMongoFilterVisitor.visitLambdaExpression("ANY", any.getLambdaVariable(), any.getExpression());
        };

        boolean expressionOperantRequiredExceptionThrown = false;
        boolean elementMatchOperantRequiredExceptionThrown = false;
        boolean multipleElementMatchOperantRequiredExceptionThrown = false;
        boolean allVariantTested = false;
        while (!allVariantTested) {
            try {
                allVariantTested = expressionOperantRequiredExceptionThrown && elementMatchOperantRequiredExceptionThrown && multipleElementMatchOperantRequiredExceptionThrown;
                return function.get();
            } catch (ExpressionOperantRequiredException ex) {
                expressionOperantRequiredExceptionThrown = true;
                function = () -> {
                    MongoFilterVisitor innerMongoFilterVisitor = new MongoFilterVisitor(edm,
                            MongoFilterVisitorContext.builder()
                                    .lambdaVariableAliases(Map.of(any.getLambdaVariable(), prepareMemberDocument(field)))
                                    .isLambdaAnyContext(true)
                                    .isExprMode(true)
                                    .build());
                    Bson innerObject = innerMongoFilterVisitor.visitLambdaExpression("ANY", any.getLambdaVariable(), any.getExpression());
                    return prepareExprDocumentForAnyLambdaWithExpr(innerObject, field, any.getLambdaVariable());
                };
            } catch (ElementMatchOperantRequiredException ex) {
                elementMatchOperantRequiredExceptionThrown = true;
                function = () -> {
                    MongoFilterVisitor innerMongoFilterVisitor = new MongoFilterVisitor(edm,
                            MongoFilterVisitorContext.builder()
                                    .lambdaVariableAliases(Map.of(any.getLambdaVariable(), prepareMemberDocument(field)))
                                    .isLambdaAnyContext(true)
                                    .elementMatchContext(new ElementMatchContext(field, false))
                                    .build());
                    Bson innerObject = innerMongoFilterVisitor.visitLambdaExpression("ANY", any.getLambdaVariable(), any.getExpression());
                    return prepareElementMatchDocumentForAnyLambda(innerObject, field);
                };
            } catch (MultipleElementMatchOperantRequiredException ex) {
                multipleElementMatchOperantRequiredExceptionThrown = true;
                function = () -> {
                    MongoFilterVisitor innerMongoFilterVisitor = new MongoFilterVisitor(edm,
                            MongoFilterVisitorContext.builder()
                                    .lambdaVariableAliases(Map.of(any.getLambdaVariable(), prepareMemberDocument(field)))
                                    .isLambdaAnyContext(true)
                                    .elementMatchContext(new ElementMatchContext(field, true))
                                    .build());
                    return innerMongoFilterVisitor.visitLambdaExpression("ANY", any.getLambdaVariable(), any.getExpression());
                };
            }
        }
        return null;
    }

    private Bson prepareExprDocumentForAnyLambdaWithExpr(Bson innerPart, String field, String lambdaVariable) {
        return new Document("$expr",
                new Document("$gt", Arrays.asList(
                        new Document("$size",
                                new Document("$filter",
                                        new Document("input", "$" + field)
                                                .append("as", lambdaVariable)
                                                .append("cond",
                                                        innerPart
                                                )
                                )
                        ),
                        0
                ))
        );
    }

    private Bson prepareElementMatchDocumentForAnyLambda(Bson innerPart, String field) {
        return new Document(field,
                new Document("$elemMatch",
                        innerPart
                )
        );
    }

    private Bson prepareElementMatchDocumentForAllLambda(Bson innerPart, String field) {
        return new Document(field, new Document("$not", new Document("$elemMatch",
                new Document("$not", innerPart)
        ))
        );
    }

    private List<Bson> tryExtractElementMatchDocumentForAnyLambda(Bson innerPart, String field) {
        if (innerPart.toBsonDocument().containsKey("$or")) {
            return (List<Bson>) innerPart.toBsonDocument().get("$or");
        }
        return List.of(prepareElementMatchDocumentForAnyLambda(innerPart, field));
    }

    private List<Bson> tryExtractElementMatchDocumentForAllLambdaWithAndOperator(Bson innerPart, String field) {
        if (innerPart.toBsonDocument().containsKey("$and")) {
            return (List<Bson>) innerPart.toBsonDocument().get("$and");
        }
        return List.of(prepareElementMatchDocumentForAllLambda(innerPart, field));
    }

    private List<Bson> tryExtractElementMatchDocumentForAllLambda(Bson innerPart, String field) {
        if (innerPart.toBsonDocument().containsKey("$or")) {
            return (List<Bson>) innerPart.toBsonDocument().get("$or");
        }
        return List.of(prepareElementMatchDocumentForAllLambda(innerPart, field));
    }

    private Document prepareMemberDocument(String field) {
        Optional<EdmEntityType> entityType = edm.getSchemas().get(0).getEntityTypes().stream().findFirst();
        Document result = new Document(ODATA_MEMBER_PROPERTY, field);
        if (entityType.isPresent()) {
            EdmElement property = entityType.get().getProperty(field);
            if (property != null) {
                result.append(ODATA_MEMBER_TYPE_PROPERTY, property.getType().getFullQualifiedName().toString());
            }
        }
        return result;
    }

    private Document prepareMemberDocument(String field, EdmType edmType) {
        Document result = new Document(ODATA_MEMBER_PROPERTY, field);
        result.append(ODATA_MEMBER_TYPE_PROPERTY, edmType.getFullQualifiedName().toString());
        return result;
    }

    // --- Binary operators ---
    @Override
    public Bson visitBinaryOperator(BinaryOperatorKind operator, Bson left, Bson right)
            throws ExpressionVisitException, ODataApplicationException {
        switch (operator) {
            case EQ:
                return combineEq(left, right);
            case NE:
                return combineFieldOp(left, right, Filters::ne);
            case GT:
                return combineFieldOp(left, right, Filters::gt);
            case GE:
                return combineFieldOp(left, right, Filters::gte);
            case LT:
                return combineFieldOp(left, right, Filters::lt);
            case LE:
                return combineFieldOp(left, right, Filters::lte);
            case AND:
                if ((this.context.isLambdaAnyContext() || this.context.isLambdaAllContext()) && !this.context.isExprMode() && !this.context.isElementMatchContext()) {
                    throw new ElementMatchOperantRequiredException("Required elementMatch");
                }
                if (this.context.isElementMatchContext()) {
                    if (this.context.isLambdaAllContext() && !this.context.elementMatchContext().multipleElemMatch() && !this.context.isExprMode()) {
                        throw new MultipleElementMatchOperantRequiredException("Multiple elemMatch required");
                    }
                    if (this.context.isLambdaAllContext() && this.context.elementMatchContext().multipleElemMatch()) {
                        BsonDocument leftDoc = left.toBsonDocument();
                        BsonDocument rightDoc = right.toBsonDocument();
                        Document leftPartDocument = new Document();
                        Document partPartDocument = new Document();
                        enrichDocumentWithQueryDocumentValues(leftDoc, leftPartDocument);
                        enrichDocumentWithQueryDocumentValues(rightDoc, partPartDocument);
                        List<Bson> andFilters = Streams.concat(tryExtractElementMatchDocumentForAllLambdaWithAndOperator(leftPartDocument, this.context.elementMatchContext().property())
                                        .stream(),
                                tryExtractElementMatchDocumentForAllLambdaWithAndOperator(partPartDocument, this.context.elementMatchContext().property())
                                        .stream()
                        ).toList();
                        return new Document("$and", andFilters);
                    }
                    BsonDocument leftDoc = left.toBsonDocument();
                    BsonDocument rightDoc = right.toBsonDocument();
                    Document finalDOcument = new Document();
                    Document leftPartDocument = new Document();
                    Document partPartDocument = new Document();
                    enrichDocumentWithQueryDocumentValues(leftDoc, leftPartDocument);
                    enrichDocumentWithQueryDocumentValues(rightDoc, partPartDocument);

                    // Checking keys conflict
                    if (leftPartDocument.keySet().stream().anyMatch(partPartDocument::containsKey)
                    ) {
                        throw new ExpressionOperantRequiredException("Operators duplicated!");
                    }

                    finalDOcument.putAll(leftPartDocument);
                    finalDOcument.putAll(partPartDocument);
                    return finalDOcument;
                }
                return Filters.and(left, right);
            case OR:
                if ((this.context.isLambdaAnyContext() || this.context.isLambdaAllContext()) && !this.context.isExprMode() && !this.context.isElementMatchContext()) {
                    throw new ElementMatchOperantRequiredException("Required elementMatch");
                }
                if (this.context.isElementMatchContext()) {
                    if (!this.context.elementMatchContext().multipleElemMatch()) {
                        throw new MultipleElementMatchOperantRequiredException("Multiple elemMatch required");
                    }
                    if (this.context.isLambdaAnyContext()) {
                        List<Bson> orFilters = Streams.concat(tryExtractElementMatchDocumentForAnyLambda(left, this.context.elementMatchContext().property())
                                        .stream(),
                                tryExtractElementMatchDocumentForAnyLambda(right, this.context.elementMatchContext().property())
                                        .stream()
                        ).toList();
                        return new Document("$or", orFilters);
                    } else if (this.context.isLambdaAllContext()) {
                        List<Bson> orFilters = Streams.concat(tryExtractElementMatchDocumentForAllLambda(left, this.context.elementMatchContext().property())
                                        .stream(),
                                tryExtractElementMatchDocumentForAllLambda(right, this.context.elementMatchContext().property())
                                        .stream()
                        ).toList();
                        return new Document("$or", orFilters);
                    }
                }
                return Filters.or(left, right);
            default:
                throw new UnsupportedOperationException("Operator not supported: " + operator);
        }
    }

    private void enrichDocumentWithQueryDocumentValues(BsonDocument doc, Document finalDOcument) {
        if (doc.containsKey(this.context.elementMatchContext().property())) {
            BsonValue value = doc.get(this.context.elementMatchContext().property());
            if (value.isDocument()) {
                finalDOcument.putAll(value.asDocument());
            } else {
                finalDOcument.append("$eq", value);
            }
        } else {
            finalDOcument.putAll(doc);
        }
    }

    // --- Unary operators ---
    @Override
    public Bson visitUnaryOperator(UnaryOperatorKind operator, Bson operand) {
        if (operator == UnaryOperatorKind.NOT) {
            return Filters.not(operand);
        }
        throw new UnsupportedOperationException("Unary operator not supported: " + operator);
    }

    // --- Methods (functions) ---
    @Override
    public Bson visitMethodCall(MethodKind methodCall, List<Bson> parameters) throws ExpressionVisitException {
//        if (methodCall == MethodKind.COMPUTE_AGGREGATE && parameters.size() > 2 && parameters.get(0) instanceof Literal method) {
//            // Ensure only literals are passed
//            if ("Normalize".equals(method.getText()) && parameters.get(1) instanceof Literal value) {
//                // Apply transformation in Java
//                return literal(NormalizeHelper.normalize(value.getText()));
//            } else {
//                throw new ExpressionVisitException("COMPUTE_AGGREGATE() for Normalize only accepts literal values");
//            }
//        }
        switch (parameters.size()) {
            case 1:
                return visitMethodWithOneParameter(methodCall, parameters);
            case 2:
                return visitMethodWithTwoParameters(methodCall, parameters);
            default:
                throw new UnsupportedOperationException("Method not supported: " + methodCall);
        }
    }

    private Bson visitMethodWithTwoParameters(MethodKind methodCall, List<Bson> parameters) {
        String field = extractField(parameters.get(0));
        String value = extractValue(parameters.get(1));
        switch (methodCall) {
            case STARTSWITH:
                return this.context.isExprMode() ? prepareRegexMatchExpr(field, Pattern.compile("^" + Pattern.quote(value)).pattern()) : this.context.isElementMatchContext() ? prepareRegexOperator(field, Pattern.compile("^" + Pattern.quote(value)).pattern()) : Filters.regex(field, Pattern.compile("^" + Pattern.quote(value)));
            case ENDSWITH:
                return this.context.isExprMode() ? prepareRegexMatchExpr(field, Pattern.compile(Pattern.quote(value) + "$").pattern()) : this.context.isElementMatchContext() ? prepareRegexOperator(field, Pattern.compile(Pattern.quote(value) + "$").pattern()) : Filters.regex(field, Pattern.compile(Pattern.quote(value) + "$"));
            case CONTAINS:
                return this.context.isExprMode() ? prepareRegexMatchExpr(field, Pattern.compile(Pattern.quote(value)).pattern()) : this.context.isElementMatchContext() ? prepareRegexOperator(field, Pattern.compile(Pattern.quote(value)).pattern()) : Filters.regex(field, Pattern.compile(Pattern.quote(value)));
            default:
                throw new UnsupportedOperationException("Method not supported: " + methodCall);
        }
    }

    private Bson prepareRegexOperator(String field, String regex) {
        return new Document("$regex", regex);
    }

    private Bson prepareRegexMatchExpr(String field, String regex) {
        return new Document("$regexMatch",
                new Document("input", field)
                        .append("regex", regex)
                        .append("options", "i")
        );
    }

    private Bson visitMethodWithOneParameter(MethodKind methodCall, List<Bson> parameters) {
        String field = extractField(parameters.get(0));
        Object value = extractValueObj(parameters.get(0));
        String type = extractFieldType(parameters.get(0));
        value = tryConvertValueByEdmType(value, type);
        Object passedValue = null;
        if (field != null) {
            passedValue = this.context.isExprMode() ? field : "$" + field;
        } else if (value != null) {
            passedValue = value;
        }
        ODataMongoFunctionMapper.MappedFunction mongoOperator = ODataMongoFunctionMapper.toOneArgumentMongoOperator(methodCall.toString());
        if (mongoOperator != null) {
            if ((this.context.isLambdaAnyContext() || this.context.isLambdaAllContext()) && !mongoOperator.isResultBoolean()) {
                if (!this.context.isExprMode()) {
                    throw new ExpressionOperantRequiredException("Operant [%s] mapped from [%s] requires expr".formatted(mongoOperator.mappedFunction(), methodCall.toString()));
                }
            }
            return new Document(mongoOperator.mappedFunction(), passedValue);
        } else {

            switch (methodCall) {
                case TRIM:
                    return new Document("$trim", new Document("input", value));
            }
            throw new UnsupportedOperationException("Method not supported: " + methodCall);
        }
    }

    // --- Helpers ---
    private Bson combineEq(Bson left, Bson right) {
        String field = extractField(left);
        Object value = extractValueObj(right);
        String type = extractFieldType(left);
        value = tryConvertValueByEdmType(value, type);
        if (field == null) {
            return this.context.isExprMode() ? new Document("$eq", Arrays.asList(left, value == null ? right : value)) : new Document("$expr", new Document("$eq", Arrays.asList(left, value == null ? right : value)));
        }
        return Filters.eq(field, value);
    }

    private Bson combineFieldOp(Bson left, Bson right,
                                java.util.function.BiFunction<String, Object, Bson> fn) {
        String field = extractField(left);
        Object value = extractValueObj(right);
        String type = extractFieldType(left);
        value = tryConvertValueByEdmType(value, type);
        Bson result = fn.apply(field, value);
        if (this.context.isExprMode()) {
            BsonDocument document = result.toBsonDocument();
            if (document.size() == 1 && document.containsKey(field)) {
                BsonValue operator = document.get(field);
                if (operator.isDocument()) {
                    document = operator.asDocument();
                    return new Document(document.getFirstKey(), Arrays.asList(field, value));
                }
            }
        }
        return result;
    }


    private String extractField(Bson bson) {
        BsonDocument document = bson.toBsonDocument();
        return document.containsKey(ODATA_MEMBER_PROPERTY) ? document.get(ODATA_MEMBER_PROPERTY).asString().getValue() : null;
    }

    private String extractValue(Bson bson) {
        return bson.toBsonDocument().get(CUSTOM_LITERAL_VALUE_PROPERTY).asString().getValue();
    }

    private Object extractValueObj(Bson bson) {
        return bson.toBsonDocument().get(CUSTOM_LITERAL_VALUE_PROPERTY);
    }

    private String extractFieldType(Bson field) {
        BsonDocument document = field.toBsonDocument();
        return document.containsKey(ODATA_MEMBER_TYPE_PROPERTY) ? document.get(ODATA_MEMBER_TYPE_PROPERTY).asString().getValue() : null;
    }

    private Object tryConvertValueByEdmType(Object value, String type) {
        if (value instanceof String && type != null) {
            return ODataToBsonConverter.toBsonValue((String) value, type);
        } else if (value instanceof BsonString && type != null) {
            return ODataToBsonConverter.toBsonValue(((BsonString) value).asString().getValue(), type);
        }
        return value;
    }

    // --- Not used in this example ---
    @Override
    public Bson visitAlias(String aliasName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bson visitTypeLiteral(EdmType edmType) throws ExpressionVisitException, ODataApplicationException {
        return null;
    }

    @Override
    public Bson visitLambdaExpression(String lambdaFunction, String lambdaVariable, Expression expression) {
        try {
            // Visit the inner expression
            Bson inner = expression.accept(this);
            return inner;

        } catch (ExpressionVisitException | ODataApplicationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Bson visitLambdaReference(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bson visitEnum(EdmEnumType edmEnumType, List<String> list) throws ExpressionVisitException, ODataApplicationException {
        return null;
    }

    @Override
    public Bson visitBinaryOperator(BinaryOperatorKind operator, Bson left, List<Bson> list) throws ExpressionVisitException, ODataApplicationException {
        switch (operator) {
            case IN:
                String field = extractField(left);
                String type = extractFieldType(left);
                List<Object> values = list.stream().map(this::extractValueObj).map(v -> tryConvertValueByEdmType(v, type)).toList();
//                if (field == null) {
//                    return this.context.isExprMode() ? new Document("$in", ) : new Document("$expr", new Document("$eq", Arrays.asList(left, value == null ? right : value)));
//                }
                return Filters.in(field, values);
            default:
                throw new UnsupportedOperationException("Operator not supported: " + operator);
        }
    }

    public record ElementMatchContext(String property, boolean multipleElemMatch) {
    }

    public record MongoFilterVisitorContext(boolean isLambdaAnyContext, Map<String, Bson> lambdaVariableAliases,
                                            boolean isExprMode, ElementMatchContext elementMatchContext,
                                            boolean isLambdaAllContext) {

        public boolean isElementMatchContext() {
            return elementMatchContext != null;
        }
    }

    private static class ExpressionOperantRequiredException extends RuntimeException {

        public ExpressionOperantRequiredException(String message) {
            super(message);
        }
    }

    private static class ElementMatchOperantRequiredException extends RuntimeException {

        public ElementMatchOperantRequiredException(String message) {
            super(message);
        }
    }

    private static class MultipleElementMatchOperantRequiredException extends RuntimeException {

        public MultipleElementMatchOperantRequiredException(String message) {
            super(message);
        }
    }
}
