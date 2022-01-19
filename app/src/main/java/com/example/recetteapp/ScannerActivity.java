package com.example.recetteapp;


import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class ScannerActivity extends AppCompatActivity {


    public static final String GOOGLE_API_KEY = "AIzaSyBKDrtmR7i10M7QO2njLCxaOg7o3O8SuGM";
    public static final String SHEET_ID = "1Tz6JtbZ3uo_B-Dtw1mEzVR7HaM2cjvXYIClurZ1vA74";
    public static final String SCRIPT_URL = "https://script.google.com/macros/s/AKfycbyiAqciMPHNdZYVQ92jI7O8k6XuoRuU_Bq9TAjVgm1AkCEJ6XtKkMs28q_fD8fIddQv/exec";
    JSONObject postDataParams;
    private static final String TAG = "ScannerActivity";
    private static final int REQUEST_CODE_STORAGE = 100;
    private EditText password;
    private GeneralHandler generalHandler;
    Bitmap bitmap;
    MultiFormatWriter multiFormatWriter;
    final Activity activity = this;
    private String qrcode = "", qrcodeFormat = "";
    int pos = -1;
    int userPos = -1;

    ImageView ivImage;
    TextView tvName,  tvPrice;
    String userId;
    int userBalance;
    String productId;
    int productQuantity;
    Sheets sheetsService = null;

    String finalUserId, finalProductId;
    int finalBalance, finalQuantity, finalRemain;
    int finalRowUserNumber, finalRowProductNumber;
    private ProgressDialog dialog;
    boolean isRefresh = false;


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

    }

    /**
     * Standard Android on create method that gets called when the activity
     * initialized.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        generalHandler = new GeneralHandler(this);

        setContentView(R.layout.activity_scanner);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        initView();



        //If the device were rotated then restore information
        if (savedInstanceState != null) {
            qrcode = savedInstanceState.getString(STATE_QRCODE);
            qrcodeFormat = savedInstanceState.getString(STATE_QRCODEFORMAT);

            if (qrcode.equals("")) {
                zxingScan();
            } else {
                showQrImage();
                password.setVisibility(View.VISIBLE);


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

                if (size > 0) {
                    for (int i = 0; i < size; i++) {
                        if (Utils.userList.get(i).getId().equals(qrcode)) {
                            if (Utils.userList.get(i).getPassword().equals(password.getText().toString())) {
                                userPos = i;
                                deductBalanceQuantity(i);
                                break;
                            }
                        }
                        if (i == size - 1) {
                            Toast.makeText(ScannerActivity.this, "User Id OR Password not Match", Toast.LENGTH_SHORT).show();
                        }

                    }
                }
            }
        });
    }

    private void deductBalanceQuantity(int i) {

        if (Integer.parseInt(Utils.userList.get(userPos).getBalance()) >= Integer.parseInt(Utils.productList.get(pos).getPrice())) {
            // balance is greater than product price
            if (Utils.userList.get(i).getDate().equals(dateConverter())){
                if (Integer.parseInt(Utils.userList.get(userPos).getRemain()) >= Integer.parseInt(Utils.productList.get(pos).getPrice())) {
                    if (Integer.parseInt(Utils.productList.get(pos).getQuantity()) > 0) {
                        // product quantity is available
                        // To Do
                        // descrease quantity and balance
                        dialog = new ProgressDialog(ScannerActivity.this);
                        this.dialog.setMessage("Please Wait");
                        this.dialog.show();
                        finalRemain = Integer.parseInt(Utils.userList.get(i).getRemain())-Integer.parseInt(Utils.productList.get(pos).getPrice());

                        updateGoogleSheet(Utils.productList.get(pos).getId(), Utils.userList.get(i).getId(), i);

                    } else {
                        itemNotAvailableDialoge();
                    }

                } else {
                    reachLimitToday();
                }
            }else {

                    if (Integer.parseInt(Utils.productList.get(pos).getQuantity()) > 0) {
                        // product quantity is available
                        // To Do
                        // descrease quantity and balance
                        dialog = new ProgressDialog(ScannerActivity.this);
                        this.dialog.setMessage("Please Wait");
                        this.dialog.show();
                        finalRemain = Integer.parseInt(Utils.userList.get(i).getLimit())-Integer.parseInt(Utils.productList.get(pos).getPrice());

                        updateGoogleSheet(Utils.productList.get(pos).getId(), Utils.userList.get(i).getId(), i);

                    } else {
                        itemNotAvailableDialoge();
                    }

            }


        } else {
            lowBalanceDialoge();
        }


    }

    private void updateGoogleSheet(String productId, String userId, int i) {

        int quantity = Integer.parseInt(Utils.productList.get(pos).getQuantity());
        quantity = quantity - 1;
        int balance = Integer.parseInt(Utils.userList.get(i).getBalance());
        balance = balance - Integer.parseInt(Utils.productList.get(pos).getPrice());

        insertData(quantity, balance, productId, userId);


    }

    private void insertData(int quantity, int balance, String pId, String uId) {


        userId = uId;
        userBalance = balance;
        productId = pId;
        productQuantity = quantity;
        Log.d(TAG, "insertData: into Sheets: \n Quantity Remaining: " + productQuantity + "\n Balance Remaining: " + userBalance + " \n Product ID: " + productId + "\n User ID: " + userId);

        Thread thread = new Thread() {
            @Override
            public void run() {

                try {
                    updateBalance(userId, Keys.SHEET_EMPOLYEES, userBalance);

                } catch (IOException | GeneralSecurityException e) {
                    e.printStackTrace();
                }

            }
        };
        thread.start();
    }

    private int getRowIndex(String id, ValueRange response) {
        List<List<Object>> values = response.getValues();
        Log.d(TAG, "getRowIndex: " + values.get(0).get(0));
        int rowIndex = -1;

        if (values != null) {

            for (int j = 0; j < response.getValues().size() - 1; j++) {

                if (values.get(j).get(0).equals(id)) {
                    Log.d(TAG, "There is a match! i= " + j);
                    rowIndex = j;
                }
            }
        }

        return rowIndex;
    }


    public void updateBalance(String id, String sheetName, int balance) throws IOException, GeneralSecurityException {

        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory factory = JacksonFactory.getDefaultInstance();

        sheetsService = new Sheets.Builder(transport, factory, null)
                .setApplicationName(getString(R.string.app_name))
                .build();

        ValueRange response = null;
        int numCol = -1;

        try {
            response = sheetsService.spreadsheets().values()
                    .get(SHEET_ID, sheetName + "!A2:A1000")
                    .setKey(GOOGLE_API_KEY)
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (response != null) {

            numCol = response.getValues() != null ? response.getValues().size() : 0;
        }
        Log.d(TAG, "ValueRange: " + numCol);

        int rowIndex = -1;

        if (response != null) {
            rowIndex = this.getRowIndex(id, response);
            if (rowIndex != -1) {
                Log.d(TAG, "updateObject: " + rowIndex);

                finalRowUserNumber = rowIndex + 2;
                finalBalance = balance;
                updateQuantity(productId, Keys.SHEET_PRODUCTS, productQuantity);

            } else {
                Log.d(TAG, "updateBalance: the obj dont exist in the sheet!");

            }
        }


    }

    public void updateQuantity(String id, String sheetName, int quantity) throws IOException, GeneralSecurityException {

        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory factory = JacksonFactory.getDefaultInstance();

        sheetsService = new Sheets.Builder(transport, factory, null)
                .setApplicationName(getString(R.string.app_name))
                .build();

        ValueRange response = null;
        int numCol = -1;

        try {
            response = sheetsService.spreadsheets().values()
                    .get(SHEET_ID, sheetName + "!A2:A1000")
                    .setKey(GOOGLE_API_KEY)
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (response != null) {

            numCol = response.getValues() != null ? response.getValues().size() : 0;
        }
        Log.d(TAG, "ValueRange: " + numCol);

        int rowIndex = 0;

        if (response != null) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    finalRowProductNumber = Integer.parseInt(Utils.productList.get(pos).getId()) + 1;
                    finalQuantity = quantity;
                    setDataIntoJson();
                }
            });
//            rowIndex = this.getRowProdcutIndex(id, response);
//            if (rowIndex != -1) {
//                Log.d(TAG, "updateObject: " + rowIndex);
//
//
//                int finalRowIndex = rowIndex;
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        finalRowProductNumber = finalRowIndex + 2;
//                        finalQuantity = quantity;
//                        setDataIntoJson();
//                    }
//                });
//
//
//            } else {
//                Log.d(TAG, "updateQuantity: the obj dont exist in the sheet!");
//            }
        }


    }


    public void successfullyDialoge() {
        new AlertDialog.Builder(ScannerActivity.this)
                .setTitle("Successfully")
                .setMessage("Your Transation is completed")
                .setCancelable(false)

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        isStoragePermissionGranted();
//                        startActivity(new Intent(ScannerActivity.this, MainActivity.class));
//                        finish();
                    }
                })

                .setIcon(R.drawable.ic_baseline_check_circle_outline_24)
                .show();
    }

    public boolean isStoragePermissionGranted() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                Log.v(TAG, "Permission is granted");
                createPdf();
                return true;
            }
            else {
                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
                return false;
            }

        } else {

            Log.v(TAG, "Permission is revoked");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
                    //resume tasks needing this permission
                    break;
                }


            case 2:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
                    //resume tasks needing this permission
                    break;
                }



        }
    }

    public void itemNotAvailableDialoge() {
        new AlertDialog.Builder(ScannerActivity.this)
                .setTitle("😥 Sorry")
                .setMessage("Item Not Available")
                .setCancelable(false)

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        startActivity(new Intent(ScannerActivity.this, MainActivity.class));
                        finish();
                    }
                })

                .setIcon(R.drawable.ic_baseline_do_disturb_24)
                .show();
    }

    public void reachLimitToday() {
        new AlertDialog.Builder(ScannerActivity.this)
                .setTitle("💰 Sorry")
                .setMessage("Your Daily limit reach")
                .setCancelable(false)

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        startActivity(new Intent(ScannerActivity.this, MainActivity.class));
                        finish();
                    }
                })

                .setIcon(R.drawable.ic_baseline_attach_money_24)
                .show();
    }

    public void lowBalanceDialoge() {
        new AlertDialog.Builder(ScannerActivity.this)
                .setTitle("💰 Sorry")
                .setMessage("Your Balance is Low")
                .setCancelable(false)

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        startActivity(new Intent(ScannerActivity.this, MainActivity.class));
                        finish();
                    }
                })

                .setIcon(R.drawable.ic_baseline_attach_money_24)
                .show();
    }

    private void initView() {

        password = findViewById(R.id.etPassword);

        ivImage = findViewById(R.id.ivImage);
        tvName = findViewById(R.id.tvName);
        tvPrice = findViewById(R.id.tvPrice);

    }

    @Override
    protected void onResume() {
        super.onResume();



        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            pos = extras.getInt("pos");

            Glide.with(this).load("https://drive.google.com/uc?export=view&id=" + Utils.productList.get(pos).getImages()).into(ivImage); // for one drive images ....

            tvName.setText(Utils.productList.get(pos).getName());
            tvPrice.setText(" $ " + Utils.productList.get(pos).getPrice());


            Log.d("TAG", "onResume: " + Utils.productList.get(pos).getName());

        }

        if (isRefresh) {
            startActivity(new Intent(ScannerActivity.this, MainActivity.class));
            isRefresh = false;
            finish();
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

            if (InternetConnection.checkConnection(ScannerActivity.this)) {
                new GetUserInfo().execute();
            } else {
                Toast.makeText(ScannerActivity.this, "Internet Connection Not Available", Toast.LENGTH_SHORT).show();
            }


        } catch (Exception e) {

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

                    showQrImage();
                    password.setVisibility(View.VISIBLE);


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
                password.setVisibility(View.VISIBLE);
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

    public String dateConverter() {

       Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        return simpleDateFormat.format(calendar.getTime()).toString();
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
            dialog.setTitle("Please Wait..." );
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
                            Utils.userList.clear();
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
                                model.setBalance(innerObject.getString(Keys.KEY_EMPOLYEES_BALANCE));
                                model.setDate(innerObject.getString(Keys.KEY_EMPOLYEES_DATE));
                                model.setLimit(innerObject.getString(Keys.KEY_EMPOLYEES_LIMIT));
                                model.setRemain(innerObject.getString(Keys.KEY_EMPOLYEES_REMAIN));


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
            if (dialog.isShowing()){

                dialog.dismiss();
            }
            /**
             * Checking if List size if more than zero then
             * Update ListView
             */
            if (Utils.userList.size() > 0) {
                Log.d(TAG, "onPostExecute: " + Utils.userList.size());


            } else {
                Toast.makeText(ScannerActivity.this, "No Data Found", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void setDataIntoJson() {
        postDataParams = new JSONObject();
        try {
            String dateTime = dateConverter();
            postDataParams.put("id", SHEET_ID);
            postDataParams.put("userId", finalRowUserNumber);
            postDataParams.put("balance", finalBalance);
            postDataParams.put("productId", finalRowProductNumber);
            postDataParams.put("quantity", finalQuantity);
            postDataParams.put("date", dateTime);
            postDataParams.put("remain", finalRemain);

            postDataParams.put("uname",Utils.userList.get(userPos).getName());
            postDataParams.put("price",Utils.productList.get(pos).getPrice());
            postDataParams.put("pname",Utils.productList.get(pos).getName());

            new SendRequest().execute();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class SendRequest extends AsyncTask<String, Void, String> {


        protected void onPreExecute() {

        }

        protected String doInBackground(String... arg0) {

            try {

                URL url = new URL(SCRIPT_URL);


                Log.e("params", postDataParams.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                String postDataString = getPostDataString(postDataParams);
                Log.d(TAG, "doInBackground: " + postDataString);
                writer.write(postDataString);

                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuffer sb = new StringBuffer("");
                    String line = "";

                    while ((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }

                    in.close();
                    return sb.toString();

                } else {
                    return new String("false : " + responseCode);
                }
            } catch (Exception e) {
                return new String("Exception: " + e.getMessage());
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
                successfullyDialoge();
                Log.d(TAG, "onPostExecute: " + result);
            }


        }
    }

    public String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while (itr.hasNext()) {

            String key = itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }
        return result.toString();
    }

    private void createPdf() {

        float pageWidth = 500;
        float pageHeight = 1000;


        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        Paint titlePaint = new Paint();

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder((int) pageWidth, (int) pageHeight, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();


        // add name of pizza
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titlePaint.setTextSize(20f);
        canvas.drawText("Recette App Production", pageWidth / 2, 200, titlePaint);


        // date and time
        paint.setTextAlign(Paint.Align.RIGHT);
        paint.setTextSize(16f);
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yy");
        canvas.drawText("Date: " + simpleDateFormat.format(date), pageWidth - 20, 300, paint);
        SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("HH:mm:ss");
        canvas.drawText("Time: " + simpleTimeFormat.format(date), pageWidth - 20, 350, paint);

        // draw qty prices etc...
        canvas.drawText("1", 40, 500, paint);
        canvas.drawText(Utils.productList.get(pos).getName(), 200, 500, paint);
        canvas.drawText(Utils.productList.get(pos).getPrice(), 700, 500, paint);
        canvas.drawText("1", 900, 500, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(Utils.productList.get(pos).getPrice(), pageWidth - 40, 500, paint);


        paint.setColor(Color.BLACK);
        paint.setTextSize(30f);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Total", 700, 700, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(Utils.productList.get(pos).getPrice(), pageWidth - 40, 700, paint);

        pdfDocument.finishPage(page);


        String fileName = "User" + Calendar.getInstance().getTimeInMillis()+ ".pdf";
        File file = new File(android.os.Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/" + getString(R.string.app_name) + "/", fileName);
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                pdfDocument.writeTo(new FileOutputStream(file));
            }else {
                pdfDocument.writeTo(new FileOutputStream(file));

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        pdfDocument.close();
        printPDF(fileName);



        isRefresh = true;

    }


    private void printPDF(String fileName) {
        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
        try {
            PrintDocumentAdapter printDocumentAdapter = new PdfDocumentAdapter(ScannerActivity.this, Common.getAppPath(ScannerActivity.this) + "" + fileName);
            printManager.print("Document", printDocumentAdapter, new PrintAttributes.Builder().build());

        } catch (Exception e) {
            Log.d(TAG, "printPDF: " + e.getMessage());
        }

    }

}