package b51.heritage.ca.bitmarketanalyzer;

import android.content.Context;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cz.msebera.android.httpclient.Header;
public class CurrenciesActivity extends AppCompatActivity {

    private ListView lvCurrencies;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currencies);

        lvCurrencies = (ListView)findViewById(R.id.lvCurrencies);
        populateCurrencyList();

        final DatabaseHandler db = new DatabaseHandler(this);

        lvCurrencies.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String wholeText = (String) parent.getItemAtPosition(position);
                String code = wholeText.substring(0, 3);

                String symbol = wholeText.substring(wholeText.indexOf("(") + 1, wholeText.indexOf(")"));

                Currency currency = new Currency(code, symbol);

                //Add new favourite currency to database for easy retrieval if hasn't already been added
                boolean added = db.addCurrency(currency);

                //If selected currency is already part of favourites in DB, display toasty message.
                if(!added){
                    Toast.makeText(getApplicationContext(), R.string.alreadyInFavs, Toast.LENGTH_LONG).show();
                }

                else{
                    Toast.makeText(getApplicationContext(), R.string.addedToFavs, Toast.LENGTH_LONG).show();
                }

                finish();
            }
        });
    }

    private void populateCurrencyList(){
        HttpUtils.get("https://blockchain.info/ticker", null, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                Iterator<String> keys = response.keys();

                List<String> currencies = new ArrayList<String>();

                try{
                    while(keys.hasNext()){

                        //The currency's short name (Ex: Euro --> EUR)
                        String key = keys.next();

                        currencies.add(key + " (" + response.getJSONObject(key).getString("symbol") + ")");
                    }

                    ArrayAdapter<String> adapter = new CustomListAdapter(getApplicationContext(), R.layout.listitem_layout, currencies);
                    lvCurrencies.setAdapter(adapter);
                }catch (JSONException e){
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_currencies, menu);
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

    private class CustomListAdapter extends ArrayAdapter {

        private Context mContext;
        private int id;
        private List <String>items ;

        public CustomListAdapter(Context context, int textViewResourceId , List<String> list )
        {
            super(context, textViewResourceId, list);
            mContext = context;
            id = textViewResourceId;
            items = list ;
        }

        @Override
        public View getView(int position, View v, ViewGroup parent)
        {
            View mView = v ;
            if(mView == null){
                LayoutInflater vi = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                mView = vi.inflate(id, null);
            }

            TextView text = (TextView) mView.findViewById(R.id.tvItem);

            if(items.get(position) != null )
            {
                text.setTextColor(Color.BLACK);
                text.setText(items.get(position));

            }

            return mView;
        }

    }
}
