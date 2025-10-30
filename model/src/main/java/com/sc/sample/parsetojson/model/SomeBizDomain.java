package com.sc.sample.parsetojson.model;

import java.util.List;
import java.util.UUID;

public record SomeBizDomain(UUID uuid, String id, String name, String likes,
                            String transport, double avgSpeed,
                            double topSpeed) implements DateFieldMappingDef  {

    private static final List<DataFieldMapping> DATA_FIELD_MAPPING =
            List.of(SomeDataFieldMapping.values());

    @Override
    public List<DataFieldMapping> getDataFieldMapping(){
        return DATA_FIELD_MAPPING;
    }

    @Override
    public String toString() {
        return "SomeBizDomain{" +
                "uuid='" + uuid + '\'' +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", likes='" + likes + '\'' +
                ", transport='" + transport + '\'' +
                ", avgSpeed=" + avgSpeed +
                ", topSpeed=" + topSpeed +
                '}';
    }

    public static enum SomeDataFieldMapping implements DataFieldMapping {
        UU_ID(0, "UUID"),
        ID(1, "ID"),
        NAME(2, "Name"),
        LIKES(3, "Likes"),
        TRANSPORT(4, "Transport"),
        AVG_SPEED(5, "Avg Speed"),
        TOP_SPEED(6, "Top Speed");

        private final String attributeLabel;
        private final int index;

        SomeDataFieldMapping(int index, String attributeLabel) {
            this.index = index;
            this.attributeLabel = attributeLabel;
        }

        @Override
        public String getAttributeLabel() {
            return attributeLabel;
        }

        @Override
        public int getIndex() {
            return index;
        }
    }
}
