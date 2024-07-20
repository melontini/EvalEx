package com.ezylang.evalex.parser;

import com.ezylang.evalex.data.EvaluationValue;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class InlinedASTNode extends ASTNode {

    EvaluationValue value;

    public InlinedASTNode(Token token, EvaluationValue value, ASTNode... parameters) {
        super(token, parameters);
        this.value = value;
    }
}
