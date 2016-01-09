package b51.heritage.ca.bitmarketanalyzer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private enum WhichChecked{
        To,
        From
    }

    private TextView tvBitcoinValue, tvCurrencySymbol, tvCurrencyName;
    private String currencySymbol, currencyName;
    private double currentBTCValue;
    private WhichChecked checkedConversion;
    private EditText txtCurrencyInput, txtBtcInput;

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putDouble("btcVal", currentBTCValue);
        savedInstanceState.putString("currencySymbol", currencySymbol);
        savedInstanceState.putSerializable("checkedConversion", checkedConversion);
        savedInstanceState.putString("currencyName", currencyName);

        savedInstanceState.putString("currencyInput", txtCurrencyInput.getText().toString());
        savedInstanceState.putString("btcInput", txtBtcInput.getText().toString());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        tvBitcoinValue = (TextView)findViewById(R.id.tvCurrentBTCVal);
        tvCurrencySymbol = (TextView)findViewById(R.id.tvCurrencySymbol);
        tvCurrencyName = (TextView)findViewById(R.id.tvCurrencyName);

        SharedPreferences prefs = getSharedPreferences("currencyPrefs", MODE_PRIVATE);

        currencyName = prefs.getString("currCode", "CAD");

        RadioGroup grpConversion = (RadioGroup)findViewById(R.id.grpConversion);
        RadioButton rdoTo = (RadioButton)findViewById(R.id.rdoTo);
        RadioButton rdoFrom = (RadioButton)findViewById(R.id.rdoFrom);

        txtCurrencyInput = (EditText)findViewById(R.id.txtCurrencyInput);
        txtBtcInput = (EditText)findViewById(R.id.txtBtcInput);

        //Load previous state, if any
        if(savedInstanceState != null){
            currentBTCValue = savedInstanceState.getDouble("btcVal");
            currencySymbol = savedInstanceState.getString("currencySymbol");
            checkedConversion = (WhichChecked)savedInstanceState.getSerializable("checkedConversion");
            currencyName = savedInstanceState.getString("currencyName");

            tvBitcoinValue.setText(String.valueOf(currentBTCValue));
            tvCurrencySymbol.setText(currencySymbol);

            txtCurrencyInput.setText(savedInstanceState.getString("currencyInput"));
            txtBtcInput.setText(savedInstanceState.getString("btcInput"));

            String newCurrName = getString(R.string.currName, currencyName);
            tvCurrencyName.setText(newCurrName);

            //Sets the conversion direction radio buttons to the appropriate states.
            if(checkedConversion == WhichChecked.To){
                rdoTo.setChecked(true);
                rdoFrom.setChecked(false);

                setDefaultRadioGroupSettings();
            }

            else if(checkedConversion == WhichChecked.From){
                rdoTo.setChecked(false);
                rdoFrom.setChecked(true);

                setAlternateRadioGroupSettings();
            }
        }

        //If no previous state
        else{
            rdoTo.setChecked(true);
            checkedConversion = WhichChecked.To;

            clearFields();
            setDefaultRadioGroupSettings();

            updateBitcoinValue();
        }


        grpConversion.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.rdoTo){
                    checkedConversion = WhichChecked.To;

                    clearFields();
                    setDefaultRadioGroupSettings();
                }

                else if(checkedId == R.id.rdoFrom){
                    checkedConversion = WhichChecked.From;

                    clearFields();
                    setAlternateRadioGroupSettings();
                }
            }
        });


        Button btnConvert = (Button)findViewById(R.id.btnConvert);
        btnConvert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkedConversion == WhichChecked.To){
                    String currencyInput = txtCurrencyInput.getText().toString();

                    //Tests empty currency field
                    if(!currencyInput.equals("")){

                        double currencyVal;

                        //Tests that currency field is only a number
                        try{
                            currencyVal = Double.parseDouble(currencyInput);
                            convertToBTC(currencyVal);

                        }catch (NumberFormatException e){
                            displayInvalidInputError("Currency");
                        }
                    }

                    else{
                        displayEmptyFieldError("Currency");
                    }
                }

                else if(checkedConversion == WhichChecked.From){
                    String btcInput = txtBtcInput.getText().toString();

                    //Tests empty BTC field
                    if(!btcInput.equals("")){

                        double btcVal;

                        //Tests that BTC field is only a number
                        try{
                            btcVal = Double.parseDouble(btcInput);
                            convertToCurrency(btcVal);

                        }catch (NumberFormatException e){
                            displayInvalidInputError("BTC");
                        }

                    }

                    else{
                        displayEmptyFieldError("BTC");
                    }
                }
            }

            private void displayEmptyFieldError(String field){
                Toast.makeText(getApplicationContext(), getString(R.string.emptyFieldMsg, field), Toast.LENGTH_LONG).show();
            }

            private void displayInvalidInputError(String field){
                Toast.makeText(getApplicationContext(), getString(R.string.notNumFieldMsg, field), Toast.LENGTH_LONG).show();
            }
        });
    }

    //Changes settings if the "To" radio button (default) is checked.
    private void setDefaultRadioGroupSettings(){
        txtCurrencyInput.requestFocus();

        txtCurrencyInput.setEnabled(true);
        txtBtcInput.setEnabled(false);
    }

    //Changes settings if the "From" radio button is checked.
    private void setAlternateRadioGroupSettings(){
        txtBtcInput.requestFocus();

        txtCurrencyInput.setEnabled(false);
        txtBtcInput.setEnabled(true);
    }

    private void clearFields(){
        txtCurrencyInput.setText("");
        txtBtcInput.setText("");
    }


    private void convertToCurrency(double btc){
        double currencyValue = btc * currentBTCValue;

        txtCurrencyInput.setText(String.format("%.2f", currencyValue));
    }


    /*
     * Converts from the specified currency to BTC using the Blockchain.info API.
     */
    private void convertToBTC(double currency){
        RequestParams params = new RequestParams();
        params.add("currency", currencyName);
        params.add("value", String.valueOf(currency));

        HttpUtils.get("https://blockchain.info/tobtc", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody, StandardCharsets.UTF_8);
                txtBtcInput.setText(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(getApplicationContext(), getString(R.string.noInternet), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateBitcoinValue(){
        HttpUtils.get("https://blockchain.info/ticker", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                try {
                    currentBTCValue = response.getJSONObject(currencyName).getDouble("buy");
                    currencySymbol = response.getJSONObject(currencyName).getString("symbol");

                    tvBitcoinValue.setText(String.valueOf(currentBTCValue));
                    tvCurrencySymbol.setText(currencySymbol);

                    String newCurrName = getString(R.string.currName, currencyName);
                    tvCurrencyName.setText(newCurrName);

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

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2: //If Chart is selected
                    Intent intent = new Intent(getApplicationContext(), ChartsMainActivity.class);
                    startActivity(intent);
                break;
            case 3: //If Latest Transactions is selected
                Intent intent1 = new Intent(getApplicationContext(), LatestTransactionsActivity.class);
                startActivity(intent1);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_main_refresh) {
            updateBitcoinValue();
            return true;
        }

        else if(id == R.id.action_select_currency){
            final View dialogView = (LayoutInflater.from(MainActivity.this)).inflate(R.layout.currency_dialog, null);

            List<String> currs = new ArrayList<String>();

            //Fetch current currencies to populate spinner
            DatabaseHandler db = new DatabaseHandler(MainActivity.this);
            List<Currency> currList = db.getCurrencies();

            for(Currency curr : currList){
                currs.add(curr.getCode());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, currs);

            Button btnAddCurrency = (Button)dialogView.findViewById(R.id.btnAdd);
            final Spinner spnrCurrency = (Spinner)dialogView.findViewById(R.id.spnrCurrency);

            spnrCurrency.setAdapter(adapter);

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setView(dialogView);

            builder.setTitle(R.string.dialogTitle)
                    .setPositiveButton(R.string.set, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String currCode = spnrCurrency.getSelectedItem().toString();

                            SharedPreferences.Editor sharedPrefs = getSharedPreferences("currencyPrefs", MODE_PRIVATE).edit();
                            sharedPrefs.putString("currCode", currCode);
                            sharedPrefs.apply();

                            currencyName = currCode;
                            updateBitcoinValue();
                        }
                    });

            final AlertDialog dialog = builder.create();

            dialog.show();

            //Add Currency button listener
            btnAddCurrency.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), CurrenciesActivity.class);
                    startActivity(intent);

                    //Close dialog so that user must re-open dialog to get new list
                    dialog.dismiss();
                }
            });

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

}
