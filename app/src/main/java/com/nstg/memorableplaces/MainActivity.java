package com.nstg.memorableplaces;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView listView;

    static ArrayList<String> places = new ArrayList<>();
    static ArrayList<LatLng> locations = new ArrayList<>();
    static ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView)findViewById(R.id.listView);

        ArrayList<String> latitude = new ArrayList<>();
        ArrayList<String> longitude = new ArrayList<>();

        SharedPreferences sharedPreferences = this.getSharedPreferences("com.nstg.memorableplaces", Context.MODE_PRIVATE);

        places.clear();
        latitude.clear();
        longitude.clear();
        locations.clear();

        try {
            places = (ArrayList)ObjectSerializer.deserialize(sharedPreferences.getString("places",ObjectSerializer.serialize(new ArrayList<String>())));
            latitude = (ArrayList)ObjectSerializer.deserialize(sharedPreferences.getString("latitude",ObjectSerializer.serialize(new ArrayList<String>())));
            longitude = (ArrayList)ObjectSerializer.deserialize(sharedPreferences.getString("longitude",ObjectSerializer.serialize(new ArrayList<String>())));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(places.size()>0 && latitude.size()>0 && longitude.size()>0){

            if(places.size() == latitude.size() && latitude.size() == longitude.size()){

                for(int i = 0; i< latitude.size(); i++){

                    locations.add(new LatLng(Double.parseDouble(latitude.get(i)), Double.parseDouble(longitude.get(i))));

                }

            }

        }else{
            places.add("Add a new place...");
            locations.add(new LatLng(0,0));
        }


        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1,places);

        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(),MapsActivity.class);
                intent.putExtra("PlaceNumber",position);
                startActivity(intent);
            }
        });

    }
}
