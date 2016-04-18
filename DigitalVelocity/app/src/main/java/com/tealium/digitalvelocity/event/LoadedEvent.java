package com.tealium.digitalvelocity.event;

import android.graphics.Bitmap;

import com.tealium.digitalvelocity.data.gson.AgendaItem;
import com.tealium.digitalvelocity.data.gson.Coordinates;
import com.tealium.digitalvelocity.data.gson.Floor;
import com.tealium.digitalvelocity.data.gson.Notification;
import com.tealium.digitalvelocity.data.gson.Question;
import com.tealium.digitalvelocity.data.gson.Sponsor;
import com.tealium.digitalvelocity.data.gson.Survey;

import java.util.Collection;
import java.util.Collections;

public class LoadedEvent {


    public static final class AgendaItemData {
        private final AgendaItem item;

        public AgendaItemData(AgendaItem item) {
            this.item = item;
        }

        public AgendaItem getItem() {
            return item;
        }
    }

    private static class DataList<T> {
        private final Collection<T> items;

        private DataList(Collection<T> items) {
            this.items = Collections.unmodifiableCollection(items);
        }

        public Collection<T> getItems() {
            return items;
        }
    }

    public static final class Surveys extends DataList<Survey> {
        public Surveys(Collection<Survey> items) {
            super(items);
        }
    }

    public static final class Questions extends DataList<Question> {
        public Questions(Collection<Question> items) {
            super(items);
        }
    }

    public static final class Sponsors extends DataList<Sponsor> {
        public Sponsors(Collection<Sponsor> items) {
            super(items);
        }
    }

    public static final class Notifications extends DataList<Notification> {
        public Notifications(Collection<Notification> items) {
            super(items);
        }
    }

    public static final class Agenda extends DataList<AgendaItem> {

        final long latestUpdatedAt;

        public Agenda(Collection<AgendaItem> items, final long latestUpdatedAt) {
            super(items);
            this.latestUpdatedAt = latestUpdatedAt;
        }

        /**
         * Get the latest updated date of the loaded agenda items.
         */
        public long getLatestUpdatedAt() {
            return latestUpdatedAt;
        }
    }

    public static final class CoordinateData extends DataList<Coordinates> {
        public CoordinateData(Collection<Coordinates> items) {
            super(items);
        }
    }

    public static final class Floors extends DataList<Floor> {
        public Floors(Collection<Floor> items) {
            super(items);
        }
    }

    private static class Image<T> {
        private final Bitmap bitmap;
        private final T item;

        private Image(Bitmap bitmap, T item) {
            this.bitmap = bitmap;
            this.item = item;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

        public T getItem() {
            return item;
        }
    }

    public static final class SponsorLogo extends Image<Sponsor> {
        public SponsorLogo(Bitmap bitmap, Sponsor item) {
            super(bitmap, item);
        }
    }

    public static final class FloorImage extends Image<Floor> {
        public FloorImage(Bitmap bitmap, Floor item) {
            super(bitmap, item);
        }
    }
}
