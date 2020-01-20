package com.xumak.grid.jcr;

import javax.jcr.Value;
import javax.jcr.ValueFactory;

/**
 * Created by j.amorataya on 3/14/16.
 */
public final class GenericValue {
    /**
     * Private constructor.
     */
    private GenericValue() {

    }

    /**
     * Get {@link Value} from a Object identified and cast the correct Value.
     *
     * @param valueFactory in order to create de Value
     * @param valueObject  value to store in the Value Object
     * @return the value to be stored
     */
    public static Value getValue(ValueFactory valueFactory, Object valueObject) {
        Value value;
        if (valueObject instanceof String) {
            value = valueFactory.createValue((String) valueObject);
        } else if (valueObject instanceof Boolean) {
            value = valueFactory.createValue((Boolean) valueObject);

        } else {
            value = null;
        }
        return value;
    }

}
