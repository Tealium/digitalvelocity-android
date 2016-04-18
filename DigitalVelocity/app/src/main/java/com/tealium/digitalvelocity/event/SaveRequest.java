package com.tealium.digitalvelocity.event;

public class SaveRequest<T> {
    private final T item;

    private SaveRequest(T item) {
        this.item = item;
    }

    public T getItem() {
        return item;
    }

    public static final class Sponsor extends SaveRequest<com.tealium.digitalvelocity.data.gson.Sponsor> {
        public Sponsor(com.tealium.digitalvelocity.data.gson.Sponsor item) {
            super(item);
        }
    }

    public static final class Notification extends SaveRequest<com.tealium.digitalvelocity.data.gson.Notification> {
        public Notification(com.tealium.digitalvelocity.data.gson.Notification item) {
            super(item);
        }
    }

    public static final class Floor extends SaveRequest<com.tealium.digitalvelocity.data.gson.Floor> {
        public Floor(com.tealium.digitalvelocity.data.gson.Floor item) {
            super(item);
        }
    }

    public static final class Coordinates extends SaveRequest<com.tealium.digitalvelocity.data.gson.Coordinates> {
        public Coordinates(com.tealium.digitalvelocity.data.gson.Coordinates item) {
            super(item);
        }
    }

    public static final class AgendaItem extends SaveRequest<com.tealium.digitalvelocity.data.gson.AgendaItem> {
        public AgendaItem(com.tealium.digitalvelocity.data.gson.AgendaItem item) {
            super(item);
        }
    }

    public static final class Survey extends SaveRequest<com.tealium.digitalvelocity.data.gson.Survey> {
        public Survey(com.tealium.digitalvelocity.data.gson.Survey item) {
            super(item);
        }
    }

    public static final class Question extends SaveRequest<com.tealium.digitalvelocity.data.gson.Question> {
        public Question(com.tealium.digitalvelocity.data.gson.Question item) {
            super(item);
        }
    }
}
