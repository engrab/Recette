package com.example.recetteapp;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.client.android.Intents;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.EnumMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;

public class ScannerActivity extends AppCompatActivity {

    private static final String TAG = "ScannerActivity";
    private TextView tvUserId;
    private EditText password;
    private TextView mLabelFormat;
    private ImageView codeImage;
    private GeneralHandler generalHandler;
    Bitmap bitmap;
    MultiFormatWriter multiFormatWriter;
    final Activity activity = this;
    private String qrcode = "", qrcodeFormat = "";
    int pos = -1;

    ImageView ivImage;
    TextView tvName, tvId, tvPrice, tvQuantity;


    private static final SimpleDateFormat date = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
    private static final String STATE_QRCODE = MainActivity.class.getName();
    private static final String STATE_QRCODEFORMAT = "format";


    /**
     * This method saves all data before the Activity will be destroyed
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString(STATE_QRCODE, qrcode);
        savedInstanceState.putString(STATE_QRCODEFORMAT, qrcodeFormat);
        generalHandler.loadTheme();
    }

    /**
     * Standard Android on create method that gets called when the activity
     * initialized.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        generalHandler = new GeneralHandler(this);
        generalHandler.loadTheme();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_scanner);

        initView();

        if (InternetConnection.checkConnection(ScannerActivity.this)) {
            new GetUserInfo().execute();
        } else {
            Toast.makeText(ScannerActivity.this, "Internet Connection Not Available", Toast.LENGTH_SHORT).show();
        }



        //If the device were rotated then restore information
        if (savedInstanceState != null) {
            qrcode = savedInstanceState.getString(STATE_QRCODE);
            qrcodeFormat = savedInstanceState.getString(STATE_QRCODEFORMAT);

            if (qrcode.equals("")) {
                zxingScan();
            } else {
                codeImage.setVisibility(View.VISIBLE);
                showQrImage();
                password.setVisibility(View.VISIBLE);
                mLabelFormat.setVisibility(View.VISIBLE);
                tvUserId.setText(qrcode);

            }

        } else {
            // Get intent, action and MINE type and check if the intent was started by a share to module from an other app
            Intent intent = getIntent();
            String action = intent.getAction();
            String type = intent.getType();

            if (Intent.ACTION_SEND.equals(action) && type != null) {
                if (type.toLowerCase().startsWith("image")) {
                    handleSendPicture();
                }
            } else {
                zxingScan();
            }
        }
        findViewById(R.id.btnSubmit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int size = Utils.userList.size();
                String id = tvUserId.getText().toString();
                String pass = password.getText().toString();

                if (size > 0){
                    for (int i = 0; i < size; i++){
                        if (Utils.userList.get(i).getId().equals(id)){
                            if (Utils.userList.get(i).getPassword().equals(pass)){
                                deductBalance(i);
                                Toast.makeText(ScannerActivity.this, "Successfully Transcation", Toast.LENGTH_SHORT).show();
                                break;
                            }
                        }
                    }
                }
            }
        });
    }

    private void deductBalance(int i) {


        dialoge();

    }

    public void dialoge(){
        new AlertDialog.Builder(ScannerActivity.this)
                .setTitle("Successfully")
                .setMessage("Your Transation is completed")
                .setCancelable(false)

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        startActivity(new Intent(ScannerActivity.this, MainActivity.class));
                        finish();
                    }
                })

                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void initView() {

        tvUserId = (TextView) findViewById(R.id.tvUserId);
        password = findViewById(R.id.etPassword);
        mLabelFormat = (TextView) findViewById(R.id.labelFormat);
        codeImage = (ImageView) findViewById(R.id.resultImageMain);

        ivImage = findViewById(R.id.ivImage);
        tvName = findViewById(R.id.tvName);
        tvId = findViewById(R.id.tvId);
        tvPrice = findViewById(R.id.tvPrice);
        tvQuantity = findViewById(R.id.tvQuantity);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            pos = extras.getInt("pos");

            Glide.with(this).load("https://drive.google.com/uc?export=view&id=" + Utils.productList.get(pos).getImages()).into(ivImage); // for one drive images ....

            tvName.setText(Utils.productList.get(pos).getName());
            tvId.setText(Utils.productList.get(pos).getId());
            tvPrice.setText("" + Utils.productList.get(pos).getPrice());
            tvQuantity.setText("" + Utils.productList.get(pos).getQuantity());

            Log.d("TAG", "onResume: " + Utils.productList.get(pos).getName());

        }

    }

    /**
     * This method creates a picture of the scanned qr code
     */
    private void showQrImage() {
        multiFormatWriter = new MultiFormatWriter();
        Map<EncodeHintType, Object> hintMap = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);

        hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hintMap.put(EncodeHintType.MARGIN, 1); /* default = 4 */
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        try {
            BarcodeFormat format = generalHandler.StringToBarcodeFormat(qrcodeFormat);
            BitMatrix bitMatrix = multiFormatWriter.encode(qrcode, format, 250, 250, hintMap);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            bitmap = barcodeEncoder.createBitmap(bitMatrix);
            codeImage.setImageBitmap(bitmap);
            codeImage.setEnabled(true);


        } catch (Exception e) {
            codeImage.setImageResource(R.drawable.ic_baseline_error_outline_24);
            codeImage.setEnabled(false);
        }
    }

    /**
     * This method handles the results of the scan
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                finish();
            } else {
                qrcodeFormat = result.getFormatName();
                qrcode = result.getContents();
                if (!qrcode.equals("")) {
                    codeImage.setVisibility(View.VISIBLE);
                    showQrImage();
                    password.setVisibility(View.VISIBLE);
                    mLabelFormat.setVisibility(View.VISIBLE);
                    tvUserId.setText(qrcode);


                }

            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void zxingScan() {
        IntentIntegrator integrator = new IntentIntegrator(activity);
        integrator.addExtra(Intents.Scan.SCAN_TYPE, Intents.Scan.MIXED_SCAN);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt((String) getResources().getText(R.string.xzing_label));

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String camera_setting = prefs.getString("pref_camera", "");
        if (camera_setting.equals("1")) {
            integrator.setCameraId(1);
        } else {
            integrator.setCameraId(0);
        }

        integrator.setOrientationLocked(false);
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(false);
        try {
            integrator.initiateScan();
        } catch (ArithmeticException e) {

        }
    }


    private void handleSendPicture() {
        Uri imageUri = (Uri) getIntent().getExtras().get(Intent.EXTRA_STREAM);
        InputStream imageStream = null;

        try {
            imageStream = getContentResolver().openInputStream(imageUri);
        } catch (FileNotFoundException e) {
            Toast.makeText(getApplicationContext(), getResources().getText(R.string.error_file_not_found), Toast.LENGTH_LONG);
        }

        //decoding bitmap
        Bitmap bMap = BitmapFactory.decodeStream(imageStream);
        int[] intArray = new int[bMap.getWidth() * bMap.getHeight()];
        // copy pixel data from the Bitmap into the 'intArray' array
        bMap.getPixels(intArray, 0, bMap.getWidth(), 0, 0, bMap.getWidth(),
                bMap.getHeight());

        LuminanceSource source = new RGBLuminanceSource(bMap.getWidth(),
                bMap.getHeight(), intArray);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));


        Reader reader = new MultiFormatReader();
        try {
            Hashtable<DecodeHintType, Object> decodeHints = new Hashtable<DecodeHintType, Object>();
            decodeHints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

            Result result = reader.decode(bitmap, decodeHints);
            qrcodeFormat = result.getBarcodeFormat().toString();
            qrcode = result.getText();

            if (qrcode != null) {
                codeImage.setVisibility(View.VISIBLE);
                mLabelFormat.setVisibility(View.VISIBLE);
                password.setVisibility(View.VISIBLE);
                tvUserId.setText(qrcode);
                showQrImage();

            } else {
                Toast.makeText(activity, getResources().getText(R.string.error_code_not_found), Toast.LENGTH_LONG).show();
            }
        } catch (FormatException e) {
            Toast.makeText(activity, getResources().getText(R.string.error_code_not_found), Toast.LENGTH_LONG).show();
        } catch (ChecksumException e) {
            Toast.makeText(activity, getResources().getText(R.string.error_code_not_found), Toast.LENGTH_LONG).show();
        } catch (NotFoundException e) {
            Toast.makeText(activity, getResources().getText(R.string.error_code_not_found), Toast.LENGTH_LONG).show();
        } catch (ArrayIndexOutOfBoundsException e) {
            Toast.makeText(activity, getResources().getText(R.string.error_code_not_found), Toast.LENGTH_LONG).show();
        }
    }

    public void goBack(View view) {
        startActivity(new Intent(ScannerActivity.this, MainActivity.class));
        finish();
    }

    class GetUserInfo extends AsyncTask<Void, Void, Void> {

        ProgressDialog dialog;
        int jIndex;
        int x;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            /**
             * Progress Dialog for User Interaction
             */
            Utils.userList.clear();
            x = Utils.userList.size();

            if (x == 0)
                jIndex = 0;
            else
                jIndex = x;

            dialog = new ProgressDialog(ScannerActivity.this);
            dialog.setTitle("Please Wait..." + x);
            dialog.setMessage("I am getting your Data");
            dialog.show();
        }

        @Nullable
        @Override
        protected Void doInBackground(Void... params) {

            /**
             * Getting JSON Object from Web Using okHttp
             */
            JSONObject jsonObject = JSONparser.getUserInfoFromWeb();

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
                        JSONArray array = jsonObject.getJSONArray(Keys.SHEET_EMPOLYEES);

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
                                UserInfoModel model = new UserInfoModel();

                                /**
                                 * Getting Inner Object from contacts array...
                                 * and
                                 * From that We will get Name of that Contact
                                 *
                                 */
                                JSONObject innerObject = array.getJSONObject(jIndex);
//                                String name = innerObject.getString(Keys.KEY_NAME);
//                                String country = innerObject.getString(Keys.KEY_COUNTRY);

                                model.setId(innerObject.getString(Keys.KEY_EMPOLYEES_ID));
                                model.setName(innerObject.getString(Keys.KEY_EMPOLYEES_NAME));
                                model.setEmail(innerObject.getString(Keys.KEY_EMPOLYEES_EMAIL));
                                model.setPassword(innerObject.getString(Keys.KEY_EMPOLYEES_PASSWORD));
                                model.setBalance(innerObject.getInt(Keys.KEY_EMPOLYEES_BALANCE));


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
                                Utils.userList.add(model);
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
            if (Utils.userList.size() > 0) {
                Log.d(TAG, "onPostExecute: "+Utils.userList.size());
//                adapter.notifyDataSetChanged();

            } else {
                Toast.makeText(ScannerActivity.this, "No Data Found", Toast.LENGTH_SHORT).show();
            }
        }
    }

}