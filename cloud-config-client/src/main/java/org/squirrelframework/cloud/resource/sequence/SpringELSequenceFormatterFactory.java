package org.squirrelframework.cloud.resource.sequence;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.squirrelframework.cloud.utils.CloudConfigCommon;

import java.util.Map;

/**
 * Created by kailianghe on 15/12/17.
 */
public class SpringELSequenceFormatterFactory {

    private static final LoadingCache<String, Expression> expressionCache =
            CacheBuilder.newBuilder().weakKeys()
            .build(CloudConfigCommon.EL_EXPRESSION_LOADER);

    public static SequenceFormatter createFormatter(final String formatExpression) {
        return new SequenceFormatter() {
            @Override
            public String format(Map<String, Object> variables) {
                StandardEvaluationContext simpleContext = new StandardEvaluationContext();
                simpleContext.setVariables(variables);
                return expressionCache.getUnchecked(formatExpression).getValue(simpleContext, String.class);
            }
        };
    }
}
