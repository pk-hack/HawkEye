package hawkeye.config.util;

import lombok.AllArgsConstructor;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

@AllArgsConstructor
class ListOfJson<T> implements ParameterizedType
{
    private final Class<?> wrapped;

    @Override
    public Type[] getActualTypeArguments()
    {
        return new Type[] { wrapped };
    }

    @Override
    public Type getRawType()
    {
        return List.class;
    }

    @Override
    public Type getOwnerType()
    {
        return null;
    }
}
