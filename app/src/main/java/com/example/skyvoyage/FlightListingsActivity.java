package com.example.skyvoyage;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FlightListingsActivity extends AppCompatActivity {
    private List<Flight> flightList;
    private FlightAdapter flightAdapter;
    private FlightMySQLiteOpenHelper flightMySQLiteOpenHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_listings);

        flightMySQLiteOpenHelper = new FlightMySQLiteOpenHelper(this);
        flightList = flightMySQLiteOpenHelper.queryAllFromDb();

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        flightAdapter = new FlightAdapter(this, flightList);
        recyclerView.setAdapter(flightAdapter);

        Button btnSearchByTime = findViewById(R.id.btnSearchByTime);
        btnSearchByTime.setOnClickListener(v -> showCustomTimePickerDialog());

        Button btnSearchByRoute = findViewById(R.id.btnSearchByRoute);
        btnSearchByRoute.setOnClickListener(v -> showRouteSearchDialog());
    }

    private void showCustomTimePickerDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_time_picker);

        TimePicker startTimePicker = dialog.findViewById(R.id.startTimePicker);
        TimePicker endTimePicker = dialog.findViewById(R.id.endTimePicker);
        Button btnConfirmTime = dialog.findViewById(R.id.btnConfirmTime);

        startTimePicker.setIs24HourView(true);
        endTimePicker.setIs24HourView(true);

        btnConfirmTime.setOnClickListener(v -> {
            int startHour = startTimePicker.getHour();
            int startMinute = startTimePicker.getMinute();
            int endHour = endTimePicker.getHour();
            int endMinute = endTimePicker.getMinute();

            String startTime = String.format("%02d:%02d", startHour, startMinute);
            String endTime = String.format("%02d:%02d", endHour, endMinute);

            filterFlightsByTime(startTime, endTime);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void filterFlightsByTime(String startTime, String endTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        List<Flight> filteredFlights = new ArrayList<>();

        try {
            Date startDate = sdf.parse(startTime);
            Date endDate = sdf.parse(endTime);
            for (Flight flight : flightList) {
                String[] times = flight.getTime().split(" - ");
                Date departureTime = sdf.parse(times[0]);
                if (departureTime.equals(startDate) || (departureTime.after(startDate) && departureTime.before(endDate))) {
                    filteredFlights.add(flight);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        flightAdapter.updateData(filteredFlights);
    }

    private void showRouteSearchDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_route_search);

        EditText etRoute = dialog.findViewById(R.id.etRoute);
        Button btnConfirmRoute = dialog.findViewById(R.id.btnConfirmRoute);

        btnConfirmRoute.setOnClickListener(v -> {
            String route = etRoute.getText().toString();
            filterFlightsByRoute(route);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void filterFlightsByRoute(String route) {
        List<Flight> filteredFlights = new ArrayList<>();

        // Trim the input and convert to lower case for case-insensitive comparison
        String trimmedRoute = route.trim().toLowerCase();

        for (Flight flight : flightList) {
            // Trim the route from the database and convert to lower case for comparison
            String flightRoute = flight.getRoute().trim().toLowerCase();
            if (flightRoute.contains(trimmedRoute)) {
                filteredFlights.add(flight);
            }
        }

        // Log the results for debugging
        System.out.println("Search route: " + trimmedRoute);
        System.out.println("Found " + filteredFlights.size() + " matching flights");

        // Update the RecyclerView with the filtered data
        flightAdapter.updateData(filteredFlights);
    }

}
