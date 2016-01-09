package b51.heritage.ca.bitmarketanalyzer;

import android.content.res.Configuration;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class FillChartActivity extends AppCompatActivity implements ChartFragment.OnFragmentInteractionListener{

    private GraphView btcChart;

    private int xLabelLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            finish();
        }

        setContentView(R.layout.fragment_chart);

        Bundle extras = getIntent().getExtras();

        String chartName = extras.getString("chartName");
        String chartAPIName = extras.getString("chartAPIName");

        TextView tvChartName = (TextView)findViewById(R.id.tvChartName);
        tvChartName.setText(chartName);

        btcChart = (GraphView)findViewById(R.id.btcGraph);

        xLabelLocation = 0;

        setCoordinates(chartAPIName);
    }

    //Used to plot the graph using the datapoints returned by the API call.
    private void setCoordinates(String chart){
        HttpUtils.get("https://blockchain.info/charts/" + chart + "?format=json", null, new JsonHttpResponseHandler(){

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                try {

                    List<DataPoint> dataPointList = new ArrayList<DataPoint>();

                    int datapoints = response.getJSONArray("values").length();


                    /*
                     * Sets the chart to start and end at the first and last datapoints. These lines are necessary otherwise too much padding
                     * is added to the beginning and end of graph and makes the plotted line look squished together.
                    */
                    btcChart.getViewport().setXAxisBoundsManual(true);
                    btcChart.getViewport().setMinX(response.getJSONArray("values").getJSONObject(0).getInt("x"));
                    btcChart.getViewport().setMaxX(response.getJSONArray("values").getJSONObject(datapoints - 1).getInt("x"));

                    for(int i = 0; i < datapoints; i++){
                        DataPoint point = new DataPoint(response.getJSONArray("values").getJSONObject(i).getInt("x"),
                                response.getJSONArray("values").getJSONObject(i).getDouble("y"));

                        dataPointList.add(point);
                    }

                    LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(dataPointList.toArray(new DataPoint[dataPointList.size()]));

                    btcChart.addSeries(series);

                    //Set the X axis label to a readable date format from the UNIX timestamp retrieved from the API call.
                    btcChart.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
                        @Override
                        public String formatLabel(double value, boolean isValueX) {
                            if (isValueX) {

                                String labelReturnVal = "";

                                //Condition used to only display a label every second measurement, otherwise the labels overlap.
                                if(xLabelLocation % 2 == 0) {

                                    String labelVal = super.formatLabel(value, isValueX).replace(",", "");

                                    long unixTimestap = Long.parseLong(labelVal);

                                    //Converts the UNIX timestap to a date.
                                    Date date = new Date(unixTimestap * 1000);

                                    SimpleDateFormat formatter = new SimpleDateFormat("MMM. yyyy");

                                    labelReturnVal = formatter.format(date);
                                }

                                xLabelLocation++;

                                return labelReturnVal;

                            } else {
                                // show currency for y values
                                return super.formatLabel(value, isValueX);
                            }
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {

            }

            @Override
            public void onFailure(int i, Header[] head, Throwable e, JSONObject b){
                Toast.makeText(getApplicationContext(), getString(R.string.noInternet), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_fill_chart, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();



        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
