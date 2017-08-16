package com.balanceball.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tijs on 30/07/2017.
 */

public class LeaderBoardController {

    private static final String TAG = LeaderBoardController.class.getSimpleName();

    public interface FetchLeaderBoardListener {
        void onSuccess(Array<LeaderBoardEntry> leaderBoardEntries);
        void onFailed();
    }

    public interface AddScoreListener {
        void onSuccess();
        void onFailed();
    }

    public void fetchLeaderBoard(FetchLeaderBoardListener listener) {
        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        Net.HttpRequest httpRequest = requestBuilder.newRequest().method(Net.HttpMethods.GET).url(
                "https://www.fluffybalance.nl/api/1.0/leaderboard").build();
        Gdx.net.sendHttpRequest(httpRequest, new GetLeaderBoardResponseListener(listener));
    }

    private class GetLeaderBoardResponseListener implements Net.HttpResponseListener {
        private  FetchLeaderBoardListener mListener;

        GetLeaderBoardResponseListener(FetchLeaderBoardListener listener){
            mListener = listener;
        }

        @Override
        public void handleHttpResponse(Net.HttpResponse httpResponse) {
            String result = httpResponse.getResultAsString();

            Json json = new Json();

            ArrayList list = json.fromJson(ArrayList.class, result);
            Array<LeaderBoardEntry> leaderBoardEntries = new Array<LeaderBoardEntry>();

            if (list != null) {
                for (Object entry : list) {
                    if (entry != null && entry instanceof JsonValue) {
                        JsonValue jsonEntry = (JsonValue) entry;

                        // the reflection does not seem to work on iOS
                        // LeaderBoardEntry leaderBoardEntry = json.readValue(
                        //    LeaderBoardEntry.class, (JsonValue)entry);
                        LeaderBoardEntry leaderBoardEntry = new LeaderBoardEntry();
                        try {
                            leaderBoardEntry.name = jsonEntry.getString("name");
                            leaderBoardEntry.rank = jsonEntry.getInt("rank");
                            leaderBoardEntry.score = jsonEntry.getInt("score");
                        } catch (IllegalArgumentException e) {
                            Gdx.app.log(TAG, "failed to read json: " + e.toString());
                            return;
                        }

                        leaderBoardEntries.add(leaderBoardEntry);
                    }
                }
            }

            mListener.onSuccess(leaderBoardEntries);
        }

        @Override
        public void failed(Throwable t) {
            mListener.onFailed();
        }

        @Override
        public void cancelled() {
            // just do nothing
        }
    }

    public void addScore(AddScoreListener listener, int score, String name) {
        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("name", name);
        parameters.put("score", "" + score);

        Net.HttpRequest httpRequest = requestBuilder.newRequest()
                .method(Net.HttpMethods.POST)
                .formEncodedContent(parameters)
                .url("https://www.fluffybalance.nl/api/1.0/score")
                .build();
        Gdx.net.sendHttpRequest(httpRequest, new AddScoreResponseListener(listener));
    }

    private class AddScoreResponseListener implements Net.HttpResponseListener {
        private AddScoreListener mListener;

        public AddScoreResponseListener(AddScoreListener listener) {
            mListener = listener;
        }


        @Override
        public void handleHttpResponse(Net.HttpResponse httpResponse) {
            mListener.onSuccess();
        }

        @Override
        public void failed(Throwable t) {
            mListener.onFailed();
        }

        @Override
        public void cancelled() {
            // just do nothing
        }
    }
}
