package b51.heritage.ca.bitmarketanalyzer;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class LatestTransactionsActivity extends AppCompatActivity {

    //Defines how many of the latest transactions to select.
    private static final int TRANSACTION_LIMIT = 20;
    private ListView lvLatestTans;
    private String currencyName, currencySymbol;
    private double currentBTCValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_latest_transactions);

        SharedPreferences prefs = getSharedPreferences("currencyPrefs", MODE_PRIVATE);

        currencyName = prefs.getString("currCode", "CAD");

        lvLatestTans = (ListView)findViewById(R.id.lvLatestTrans);

        getBitcoinValue();

        lvLatestTans.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String transactValText = ((TextView)view.findViewById(R.id.tvCurrVal)).getText().toString();

                double numberValue = Double.parseDouble(transactValText.substring(transactValText.indexOf(" ") + 1));

                convertToBTC(numberValue);
                //TODO: use API to get current BTC value for the @currencyName value, fetch the symbol as well while at it. Call the convertToBTC() method to convert back to btc val.
            }
        });
    }

    private void convertToBTC(double currency){
        RequestParams params = new RequestParams();
        params.add("currency", currencyName);
        params.add("value", String.valueOf(currency));

        HttpUtils.get("https://blockchain.info/tobtc", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String value = new String(responseBody, StandardCharsets.UTF_8);

                Toast.makeText(getApplicationContext(), value + " BTC", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(getApplicationContext(), getString(R.string.noInternet), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void fillLatestTransactions(){
        HttpUtils.get("https://blockchain.info/unconfirmed-transactions?format=json", null, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                List<String> transactIDList = new ArrayList<String>();
                List<Long> bitcoinOutputList = new ArrayList<Long>();

                for(int i = 0; i < TRANSACTION_LIMIT; i++){

                    try{
                        String transactID = response.getJSONArray("txs").getJSONObject(i).getString("hash");
                        long totalOuput = 0;

                        //Use the "out" object since fees are often charged to the sender so we only want what the receiving wallets actually got.
                        JSONArray transactOutputs = response.getJSONArray("txs").getJSONObject(i).getJSONArray("out");

                        //Sums up all outputs since a transaction can send to many wallets.
                        for(int j = 0; j < transactOutputs.length(); j++){
                            totalOuput += transactOutputs.getJSONObject(j).getInt("value");
                        }

                        //Some long additions overflow and become negative. We ignore those transactions.
                        if(totalOuput >= 0){
                            transactIDList.add(transactID);
                            bitcoinOutputList.add(totalOuput);
                        }
                    }

                    catch (JSONException e){
                        e.printStackTrace();
                    }


                    ArrayAdapter<String> adapter = new LatestTransactionsAdapter(getApplicationContext(), R.layout.transactions_listitem, transactIDList, bitcoinOutputList);
                    lvLatestTans.setAdapter(adapter);
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {

            }

            @Override
            public void onFailure(int i, Header[] head, Throwable e, JSONObject b){
                Toast.makeText(getApplicationContext(), getString(R.string.noInternet), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_latest_transactions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.latest_trans_refresh) {
            fillLatestTransactions();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getBitcoinValue(){
        HttpUtils.get("https://blockchain.info/ticker", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                try {
                    currentBTCValue = response.getJSONObject(currencyName).getDouble("buy");
                    currencySymbol = response.getJSONObject(currencyName).getString("symbol");

                    fillLatestTransactions();

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
            }
        });
    }

    private double convertToCurrency(double btc){

        return btc * currentBTCValue;
    }

    private class LatestTransactionsAdapter extends ArrayAdapter {

        private Context mContext;
        private int id;
        private List<String> transactions;
        private List<Long> outputs;

        public LatestTransactionsAdapter(Context context, int textViewResourceId , List<String> transactIDs, List<Long> totalOutputs)
        {
            super(context, textViewResourceId, transactIDs);
            mContext = context;
            id = textViewResourceId;
            transactions = transactIDs;
            outputs = totalOutputs;
        }

        @Override
        public View getView(int position, View v, ViewGroup parent)
        {
            View mView = v ;
            if(mView == null){
                LayoutInflater vi = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                mView = vi.inflate(id, null);
            }

            TextView tvTransactID = (TextView) mView.findViewById(R.id.tvTransactID);
            TextView tvCurrVal = (TextView)mView.findViewById(R.id.tvCurrVal);

            if(transactions.get(position) != null )
            {
                tvTransactID.setTextColor(Color.BLACK);

                String truncatedID = transactions.get(position).substring(0, 10) + "...";

                tvTransactID.setText("ID: " + truncatedID);

                tvCurrVal.setTextColor(Color.BLACK);

                double bitcoinFromSatoshi = outputs.get(position) / 100000000.0;

                double currencyValue = convertToCurrency(bitcoinFromSatoshi);

                tvCurrVal.setText( currencySymbol + " " + String.format("%.2f", currencyValue));
            }

            return mView;
        }

    }


}
