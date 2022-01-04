package com.example.recetteapp;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {
    private ListView listView;
    private ProductArrayAdapter adapter;
    private static final String TAG = MainActivity.class.getName();

    public static FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        adapter = new ProductArrayAdapter(this, Utils.productList);

        listView = findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(MainActivity.this, ScannerActivity.class);
                intent.putExtra("pos", position);
                startActivity(intent);


//                Snackbar.make(inflate.findViewById(R.id.parentLayout), list.get(position).getBrandID() + " => " + list.get(position).getItemName(), Snackbar.LENGTH_LONG).show();
            }
        });

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View view) {

                /**
                 * Checking Internet Connection
                 */
                if (InternetConnection.checkConnection(MainActivity.this)) {
                    new GetProducts().execute();
                } else {
                    Toast.makeText(MainActivity.this, "Internet Connection Not Available", Toast.LENGTH_SHORT).show();
                }
            }
        });
        new GetProducts().execute();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (InternetConnection.checkConnection(MainActivity.this)) {
            new GetProducts().execute();
        } else {
            Toast.makeText(MainActivity.this, "Internet Connection Not Available", Toast.LENGTH_SHORT).show();
        }
    }

    class GetProducts extends AsyncTask<Void, Void, Void> {

        ProgressDialog dialog;
        int jIndex;
        int x;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            /**
             * Progress Dialog for User Interaction
             */
            Utils.productList.clear();
            x = Utils.productList.size();

            if (x == 0)
                jIndex = 0;
            else
                jIndex = x;

            dialog = new ProgressDialog(MainActivity.this);
            dialog.setTitle("Hey Wait Please..." + x);
            dialog.setMessage("I am getting your Data");
            dialog.show();
        }

        @Nullable
        @Override
        protected Void doInBackground(Void... params) {

            /**
             * Getting JSON Object from Web Using okHttp
             */
            JSONObject jsonObject = JSONparser.getProductFromWeb();

            try {
                /**
                 * Check Whether Its NULL???
                 */
                if (jsonObject != null) {
                    /**
                     * Check Length...
                     */
                    if (jsonObject.length() > 0) {
                        /**
                         * Getting Array named "contacts" From MAIN Json Object
                         */
                        JSONArray array = jsonObject.getJSONArray(Keys.SHEET_PRODUCTS);

                        /**
                         * Check Length of Array...
                         */


                        int lenArray = array.length();
                        if (lenArray > 0) {
                            for (; jIndex < lenArray; jIndex++) {

                                /**
                                 * Creating Every time New Object
                                 * and
                                 * Adding into List
                                 */
                                ProductModel model = new ProductModel();

                                /**
                                 * Getting Inner Object from contacts array...
                                 * and
                                 * From that We will get Name of that Contact
                                 *
                                 */
                                JSONObject innerObject = array.getJSONObject(jIndex);
//                                String name = innerObject.getString(Keys.KEY_NAME);
//                                String country = innerObject.getString(Keys.KEY_COUNTRY);

                                model.setId(innerObject.getString(Keys.KEY_PRODUCTS_ID));
                                model.setName(innerObject.getString(Keys.KEY_PRODUCTS_NAME));
                                model.setImages(innerObject.getString(Keys.KEY_PRODUCTS_IMAGES));
                                model.setPrice(innerObject.getInt(Keys.KEY_PRODUCTS_PRICE));
                                model.setQuantity(innerObject.getInt(Keys.KEY_PRODUCTS_QUANTITY));


                                /**
                                 * Getting Object from Object "phone"
                                 */
                                //JSONObject phoneObject = innerObject.getJSONObject(Keys.KEY_PHONE);
                                //String phone = phoneObject.getString(Keys.KEY_MOBILE);

//                                model.setName(name);
//                                model.setCountry(country);

                                /**
                                 * Adding name and phone concatenation in List...
                                 */
                                Utils.productList.add(model);
                            }
                        }
                    }
                } else {

                }
            } catch (JSONException je) {
                Log.i(JSONparser.TAG, "" + je.getLocalizedMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
            /**
             * Checking if List size if more than zero then
             * Update ListView
             */
            if (Utils.productList.size() > 0) {
                adapter.notifyDataSetChanged();

            } else {
                Toast.makeText(MainActivity.this, "No Data Found", Toast.LENGTH_SHORT).show();            }
        }
    }

}