package com.balanceball.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.IntArray;

/**
 * Created by tijs on 18/07/2017.
 */

public class HighScoreController {

    // Warning, change this will break existing high scores!
    private static final String PREFERENCES_NAME = "preferences";
    private static final String PREFERENCES_SCORE_NAME = "score_";
    private static final String PREFERENCES_USER_NAME = "name";

    public static final int MIN_NAME_LENGTH = 3;
    public static final int MAX_NAME_LENGTH = 12;

    private static int MAX_NR_SCORES = 100;

    public static IntArray fetchHighScores() {
        Preferences prefs = Gdx.app.getPreferences(PREFERENCES_NAME);

        IntArray scores = new IntArray();

        for (int i = 0; i < MAX_NR_SCORES; i++) {
            int score = prefs.getInteger(PREFERENCES_SCORE_NAME + i, -1);

            if (score == -1) {
                break;
            }

            scores.add(score);
        }

        return scores;
    }

    public static void addHighScore(int score) {
        IntArray scores = fetchHighScores();

        scores.add(score);

        scores.sort();
        scores.reverse();

        Preferences prefs = Gdx.app.getPreferences(PREFERENCES_NAME);

        for (int i = 0; i < Math.min(scores.size, MAX_NR_SCORES); ++i) {
            prefs.putInteger(PREFERENCES_SCORE_NAME + i, scores.get(i));
        }

        prefs.flush();
    }

    public static void setUserName(String name) {
        Preferences prefs = Gdx.app.getPreferences(PREFERENCES_NAME);

        // make sure the name is correct
        if (name.length() < MIN_NAME_LENGTH) {
            name = name + "---";
        } else if (name.length() > MAX_NAME_LENGTH){
            name += name.substring(0, MAX_NAME_LENGTH - 1);
        }

        prefs.putString(PREFERENCES_USER_NAME, name.trim());
        prefs.flush();
    }

    public static String getUserName() {
        Preferences prefs = Gdx.app.getPreferences(PREFERENCES_NAME);

        return prefs.getString(PREFERENCES_USER_NAME, null);
    }
}
