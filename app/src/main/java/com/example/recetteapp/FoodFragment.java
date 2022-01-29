package com.example.recetteapp;

import static com.example.recetteapp.InternetConnection.checkConnection;

import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FoodFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FoodFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private TextView noInternet;
    private RecyclerView rv;
    private ProductArrayAdapter adapter;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FoodFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FoodFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FoodFragment newInstance(String param1, String param2) {
        FoodFragment fragment = new FoodFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflate = inflater.inflate(R.layout.fragment_food, container, false);
        rv = inflate.findViewById(R.id.rv);
        noInternet = inflate.findViewById(R.id.tvNoInternet);
        return inflate;
    }

    @Override
    public void onResume() {
        super.onResume();


        if (checkConnection(requireActivity())){
            noInternet.setVisibility(View.GONE);
            new GetProducts().execute();
            adapter = new ProductArrayAdapter(requireContext(), Utils.productList);


            int orientation = this.getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                rv.setLayoutManager(new GridLayoutManager(requireContext(), 1));
                rv.setAdapter(adapter);

            } else {
                rv.setLayoutManager(new GridLayoutManager(requireContext(), 2));
                rv.setAdapter(adapter);

            }
        }else {
            noInternet.setVisibility(View.VISIBLE);
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

            dialog = new ProgressDialog(requireContext());
            dialog.setTitle("Hey Wait Please..." );
            dialog.setMessage("I am getting your Data");
            dialog.setCancelable(false);
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
                                model.setPrice(innerObject.getString(Keys.KEY_PRODUCTS_PRICE));
                                model.setQuantity(innerObject.getString(Keys.KEY_PRODUCTS_QUANTITY));


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
                Toast.makeText(requireContext(), "No Data Found", Toast.LENGTH_SHORT).show();            }
        }
    }
}