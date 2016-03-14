package com.tealium.digitalvelocity.event;


import com.tealium.digitalvelocity.data.gson.Floor;
import com.tealium.digitalvelocity.data.gson.Sponsor;

public class LoadRequest {
    private LoadRequest() {
    }


    public static final class AgendaItemData {
        private final String id;

        public AgendaItemData(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }

    public static final class Sponsors {
    }

    public static final class Notifications {
    }

    public static final class Agenda {
    }

    public static final class Coordinates {

    }

    public static final class Floors {

    }

    private static class Image<T> {
        private final T item;

        private Image(T item) {
            this.item = item;
        }

        public T getItem() {
            return item;
        }
    }

    public static final class SponsorLogo extends Image<Sponsor> {
        public SponsorLogo(Sponsor item) {
            super(item);
        }
    }

    public static final class FloorImage extends Image<Floor> {
        public FloorImage(Floor item) {
            super(item);
        }
    }
}
