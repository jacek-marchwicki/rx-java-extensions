package com.appunite.gson;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.List;

public class ImmutableListDeserializer implements JsonDeserializer<ImmutableList<?>> {

    @Override
    public ImmutableList<?> deserialize(JsonElement json,
                                        Type typeOfT,
                                        JsonDeserializationContext context) throws JsonParseException {
        @SuppressWarnings("unchecked")
        final TypeToken<ImmutableList<?>> immutableListToken = (TypeToken<ImmutableList<?>>) TypeToken.of(typeOfT);
        final TypeToken<? super ImmutableList<?>> listToken = immutableListToken.getSupertype(List.class);
        final List<?> list = context.deserialize(json, listToken.getType());
        try {
            return ImmutableList.copyOf(list);
        } catch (NullPointerException e) {
            throw new JsonParseException("Could not parse: " + json, e);
        }
    }
}
