package com.tealium.digitalvelocity.event;


import android.text.TextUtils;

import com.tealium.digitalvelocity.data.gson.Floor;
import com.tealium.digitalvelocity.data.gson.Sponsor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public static final class Surveys {

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

    public static final class Questions {

        private final List<String> mQuestionIds;

        public Questions(String[] ids) {
            final List<String> questionIds = new ArrayList<>(ids.length);
            for (int i = 0; i < ids.length; i++) {
                final String id = ids[i];
                if (TextUtils.isEmpty(id)) {
                    throw new IllegalArgumentException();
                }
                questionIds.add(id);
            }

            mQuestionIds = Collections.unmodifiableList(questionIds);
        }

        public List<String> getQuestionIds() {
            return mQuestionIds;
        }
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
