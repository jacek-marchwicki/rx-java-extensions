package com.appunite.gson;

import com.google.gson.FieldNamingStrategy;

import java.lang.reflect.Field;

public class AndroidUnderscoreNamingStrategy implements FieldNamingStrategy {

    @Override
    public String translateName(Field field) {
        final String name = field.getName();
        StringBuilder translation = new StringBuilder();
        int i = 0;
        if (name.length() >= 2) {
            if ('m' == name.charAt(0) && Character.isUpperCase(name.charAt(1))) {
                i++;
            }
        }
        for (; i < name.length(); i++) {
            char character = name.charAt(i);
            if (Character.isUpperCase(character) && translation.length() != 0) {
                translation.append("_");
            }
            translation.append(character);
        }
        return translation.toString().toLowerCase();
    }
}
