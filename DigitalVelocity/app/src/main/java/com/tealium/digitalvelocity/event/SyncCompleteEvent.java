package com.tealium.digitalvelocity.event;

public final class SyncCompleteEvent {
    private SyncCompleteEvent() {
    }

    public static final class ParseEvent {

    }

    public static final class ParseLocation {

    }

    public static final class ParseCompany {

    }

    public static final class Image {
        private final String id;

        public Image(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }
}
