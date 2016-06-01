package com.tealium.digitalvelocity.data;


import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class IOUtils {

    public static final String SUFFIX_SPONSOR = ".sponsor";
    public static final String SUFFIX_FLOOR = ".floor";
    public static final String SUFFIX_NOTIFICATION = ".notification";
    public static final String SUFFIX_COORDINATES = ".coordinates";
    public static final String SUFFIX_AGENDA_ITEM = ".agenda_item";
    public static final String SUFFIX_SURVEY = ".survey";
    public static final String SUFFIX_QUESTION = ".question";

    public static final String IMG_SUFFIX_JPG = ".jpg";
    public static final String IMG_SUFFIX_PNG = ".png";
    public static final String IMG_SUFFIX_JPEG = ".jpeg";
    public static final String IMG_SUFFIX_GIF = ".gif";

    private static final Set<String> IMG_SUFFIXES;

    static {
        Set<String> imgSuffixes = new HashSet<>(4);
        imgSuffixes.add(IMG_SUFFIX_GIF);
        imgSuffixes.add(IMG_SUFFIX_JPEG);
        imgSuffixes.add(IMG_SUFFIX_JPG);
        imgSuffixes.add(IMG_SUFFIX_PNG);

        IMG_SUFFIXES = Collections.unmodifiableSet(imgSuffixes);
    }

    private IOUtils() {
    }

    public static File createSurveyFile(Context context, String id) {
        return new File(context.getFilesDir(), id + SUFFIX_SURVEY);
    }

    public static File createQuestionFile(Context context, String id) {
        return new File(context.getFilesDir(), id + SUFFIX_QUESTION);
    }

    public static File createSponsorFile(Context context, String id) {
        return new File(context.getFilesDir(), id + SUFFIX_SPONSOR);
    }

    public static File createNotificationFile(Context context, String id) {
        return new File(context.getFilesDir(), id + SUFFIX_NOTIFICATION);
    }

    public static File createCoordsFile(Context context, String id) {
        return new File(context.getFilesDir(), id + SUFFIX_COORDINATES);
    }

    public static File createFloorFile(Context context, String id) {
        return new File(context.getFilesDir(), id + SUFFIX_FLOOR);
    }

    public static File createAgendaItemFile(Context context, String id) {
        return new File(context.getFilesDir(), id + SUFFIX_AGENDA_ITEM);
    }

    public static File getImageFile(Context context, String id) {

        if (Model.getInstance().isImageEnqueued(id)) {
            // May currently be writing.
            return null;
        }

        String name;
        for (File file : context.getFilesDir().listFiles()) {
            name = file.getName();
            if (name.startsWith(id + ".")) {
                for (String suffix : IMG_SUFFIXES) {
                    if (name.endsWith(suffix)) {
                        return file;
                    }
                }
            }
        }

        return null;
    }

    public static String readFile(File file) throws IOException {
        String contents = "";
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null) {
            contents += line;
        }
        br.close();
        return contents;
    }

    public static FilenameFilter createPurgeFilter() {

        final String[] suffixes = new String[]{
                SUFFIX_FLOOR,
                SUFFIX_AGENDA_ITEM,
                SUFFIX_COORDINATES,
                SUFFIX_NOTIFICATION,
                SUFFIX_SPONSOR,
                SUFFIX_SURVEY,
                SUFFIX_QUESTION
        };

        return new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                for (String suffix : suffixes) {
                    if (filename.endsWith(suffix)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }
}
