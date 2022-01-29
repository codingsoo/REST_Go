package io.resttestgen.nominaltester.fieldgenerator.manufacturetraces;

import java.util.List;

public class ArrayTrace extends ManufactureTrace {

    public ArrayTrace(int length, Class<?> itemsType, List<Object> value) {
        this.length = length;
        this.itemsType = itemsType;
        this.value = value;
        this.items = value.toArray();
    }

    private final int length;

    public int getLength() {
        return length;
    }

    public Class<?> getItemsType() {
        return itemsType;
    }

    public Object[] getItems() {
        return items;
    }

    private final Class<?> itemsType;
    private final List<Object> value;
    private final Object[] items;

    @Override
    public String constructionString() {
        String listName = getObjectName(value);
        String itemType = itemsType.getSimpleName();
        String repr = String.format("List<%s> %s = new ArrayList<>();", itemType, listName);
        for (Object item : items) {
            String objectName = getObjectName(item);
            repr += String.format("\n\t%s.add(%s);", listName, objectName);
        }
        return repr;
    }
}
