package com.axoninstruments.airqualitymonitor;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.androidplot.Plot;
/**
 * A simple {@link Fragment} subclass.
 */
public class GraphsFragment extends Fragment {

    private Plot graph;

    private IndoorSensorData indoorSensorData = IndoorSensorData.getInstance();
    private AirSensorData airSensorData = AirSensorData.getInstance();
    private CO2SensorData co2SensorData = CO2SensorData.getInstance();

    public GraphsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_graphs, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.graphs_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }
}
