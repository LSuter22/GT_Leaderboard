package com.example.leaderboard;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CustomAdapter.ItemClickListener {

    private RecyclerView recyclerView;
    private CustomAdapter adapter;
    private List<Pair<String, String>> orderedPairs = new ArrayList<>();
    private List<String> timesasstring = new ArrayList<>();
    private List<String> strings = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.contains("PrevOrderedList")) {
            Log.i("LastList","Found");

            Gson gson = new Gson();
            Type listFormat = new TypeToken<List<Pair<String,String>>>(){}.getType();
            String json = sharedPreferences.getString("PrevOrderedList",null);
            orderedPairs = gson.fromJson(json,listFormat);
            json = sharedPreferences.getString("TimeAsString",null);
            timesasstring = gson.fromJson(json, new TypeToken<List<String>>(){}.getType());
            json = sharedPreferences.getString("Numbers",null);
            strings = gson.fromJson(json, new TypeToken<List<String>>(){}.getType());

            for (String s : timesasstring){
                Log.i("Pairs ", s);
            }
            for (String s : strings){
                Log.i("Pairs ", s);
            }
            Log.i("LastList","Init Recycler");
            initRecyclerView();
        }else {
            Log.i("LastList","  Not Found");
            timesasstring = new ArrayList<>();
            strings = new ArrayList<>();
        }

        ImageView clearall = findViewById(R.id.binicon);
        clearall.setOnClickListener(view -> ClearReceyclerView());

        ImageView addtime = findViewById( R.id.plusicon);
        addtime.setOnClickListener(view -> addtimedialog());

        ImageView scrolltotop = findViewById(R.id.arrowup);
        scrolltotop.setOnClickListener(view -> setRecyclerViewPosition(0));

        ImageView scrolltobottom = findViewById(R.id.arrowdown);
        scrolltobottom.setOnClickListener(view -> {
            if (orderedPairs.size() >= 5) {
                setRecyclerViewPosition(orderedPairs.size() - 1);
            }
        });

        for(int i = 0; i <= 50; i++){
            add(i);
        }

    }

    private void add(int i){
        strings.add(String.valueOf(i));
        timesasstring.add("00:00:00");
        orderedPairs = getOrderedList(timesasstring,strings);
        initRecyclerView();
    }

    private void initRecyclerView(){
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(null);
        adapter = new CustomAdapter(this,orderedPairs);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(View view, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.dialog_layout, null);
        builder.setView(dialogLayout);

        EditText editText1 = dialogLayout.findViewById(R.id.nameinput);
        EditText editText2 = dialogLayout.findViewById(R.id.timeinput);

        editText1.setText(strings.get(position));
        editText2.setText(timesasstring.get(position));

        builder.setPositiveButton("Apply", (dialog, which) -> {
            strings.remove(position);
            timesasstring.remove(position);
            orderedPairs.remove(position);

            String name = editText1.getText().toString();
            String time = editText2.getText().toString();

            Log.i("Pairs",name);
            Log.i("Pairs",time);
            strings.add(name);
            timesasstring.add(time);
            orderedPairs = getOrderedList(timesasstring,strings);
            initRecyclerView();
            dialog.dismiss();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.setNeutralButton("Remove", (dialog, i) -> {
            strings.remove(position);
            timesasstring.remove(position);
            orderedPairs.remove(position);
            orderedPairs = getOrderedList(timesasstring,strings);
            initRecyclerView();
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(this,R.drawable.dialogbackground));
        dialog.show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getColor(R.color.black));
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getColor(R.color.black));
        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(getColor(R.color.black));
    }

    private void setRecyclerViewPosition(int position){
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.smoothScrollToPosition(position);
    }

    private void ClearReceyclerView(){
        if (!timesasstring.isEmpty()) {
            int size = timesasstring.size();
            timesasstring.clear();
            orderedPairs.clear();
            strings.clear();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            sharedPreferences.edit().clear().apply();
            adapter.notifyItemRangeRemoved(0, size - 1);
        }
    }

    private int numberstexttotime(String timeString){
        String[] timeArray = timeString.split(":");
        int hours = Integer.parseInt(timeArray[0]);
        int minutes = Integer.parseInt(timeArray[1]);
        int seconds = Integer.parseInt(timeArray[2]);
        int totalMilliseconds = (hours * 60 * 60 + minutes * 60 + seconds) * 1000;
        return totalMilliseconds;
    }


    private List<Pair<String, String>> getOrderedList(List<String> timesasstring, List<String> strings) {
            List<Integer> numbers = new ArrayList<>();
            for (String timeString : timesasstring) {
                try {
                    Log.i("Pairs ",timeString);
                    String[] timeArray = timeString.split(":");
                    for (String s : timeArray){
                        Log.i("Pairs ",s);
                    }
                    int hours = Integer.parseInt(timeArray[0]);
                    int minutes = Integer.parseInt(timeArray[1]);
                    int seconds = Integer.parseInt(timeArray[2]);
                    int totalMilliseconds = (hours * 60 * 60 + minutes * 60 + seconds) * 1000;
                    numbers.add(totalMilliseconds);
                }catch (NumberFormatException exception){
                    Log.i("Pairs","Exception");
                }
            }

            List<Pair<Integer, String>> pairs = new ArrayList<>();
            for (int i = 0; i < numbers.size(); i++) {
                pairs.add(new Pair<>(numbers.get(i), strings.get(i)));
            }

            Collections.sort(pairs, Comparator.comparing(pair -> pair.first));

            List<Pair<String, String>> orderedPairs = new ArrayList<>();
            for (Pair<Integer, String> pair : pairs) {
                int index = numbers.indexOf(pair.first);
                String orderedTimeString = timesasstring.get(index);
                String orderedString = pair.second;
                orderedPairs.add(new Pair<>(orderedString, orderedTimeString));
            }

            for (Pair<String, String> pair : orderedPairs) {
                Log.i("Pairs ", pair.first + " , " + pair.second);
            }

            for (String s : timesasstring) {
                Log.i("Pairs ", s);
            }

            numbers.clear();

        // Convert the ArrayList<MyPair> to a JSON string using Gson
        Gson gson = new Gson();
        String json = gson.toJson(orderedPairs);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        sharedPreferences.edit().putString("PrevOrderedList",json).apply();

        json = gson.toJson(timesasstring);
        sharedPreferences.edit().putString("TimeAsString",json).apply();

        json = gson.toJson(strings);
        sharedPreferences.edit().putString("Numbers",json).apply();

        return orderedPairs;
    }

    public void addtimedialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.dialog_layout, null);
        builder.setView(dialogLayout);

        EditText editText1 = dialogLayout.findViewById(R.id.nameinput);
        EditText editText2 = dialogLayout.findViewById(R.id.timeinput);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String name = editText1.getText().toString();
            String time = editText2.getText().toString();

            Log.i("Pairs",name);
            Log.i("Pairs",time);
            strings.add(name);
            timesasstring.add(time);
            orderedPairs = getOrderedList(timesasstring,strings);
            initRecyclerView();
            dialog.dismiss();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(this,R.drawable.dialogbackground));
        dialog.show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getColor(R.color.black));
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getColor(R.color.black));
    }

}