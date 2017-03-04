package local.hal.st32.android.ih;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;


import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;



import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


public class ProductDetailActivity extends AppCompatActivity {

    GlobalsClass globals;
    private String URL = "ProductDetailServlet";
   // private static final String URL = "http://192.168.1.108:8080/IHAndroid/ProductDetailServlet";

    ImageView productImg;
    TextView txRackName;
    TextView txProductID;
    TextView txProductName;
    TextView txMakerID;
    TextView txMakerName;
    TextView txSummary;
    String rackName;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("");
        //戻るボタン
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        String productID = intent.getStringExtra("productID");
        rackName = intent.getStringExtra("rackName");
        globals = (GlobalsClass) this.getApplication();
        URL = globals.URL(URL);
        RestAccess access = new RestAccess();
        access.execute(URL, productID);
    }

    /**
     * 検品完了
     *
     * @param view
     */
    public void checkDone(View view) {
//        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show();
        finish();
    }


    /**
     * 戻るボタン
     * 検品商品一覧に戻る
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private class RestAccess extends AsyncTask<String, String, String> {
        private static final String DEBUG_TAG = "RestAccess";

        private boolean _success = false;


        //非同期で実行したい処理
        @Override
        public String doInBackground(String... params) {
            String urlStr = params[0];
            HttpURLConnection con = null;
            InputStream is = null;
            String result = "";
            String postData = "productID=" + params[1];

            try {
                publishProgress(getString(R.string.msg_send_before));
                java.net.URL url = new URL(urlStr);
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);
                con.setDoOutput(true);
                OutputStream os = con.getOutputStream();
                os.write(postData.getBytes());
                os.flush();
                os.close();
                int status = con.getResponseCode();

                if (status != 200) {
                    throw new IOException("ステータスコード:" + status);
                }
                publishProgress(getString(R.string.msg_send_after));
                is = con.getInputStream();

                //Listに変換　resultに入れる
                result = is2String(is);
                System.out.println("result結果:" + result);
                _success = true;


            } catch (MalformedURLException ex) {
                publishProgress(getString(R.string.msg_err_send));
                Log.e(DEBUG_TAG, "URL変換失敗", ex);
            } catch (IOException ex) {
                publishProgress(getString(R.string.msg_err_send));
                Log.e(DEBUG_TAG, "通信失敗", ex);
            } finally {
                if (con != null) {
                    con.disconnect();
                }
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException ex) {
                    publishProgress(getString(R.string.msg_err_parse));
                }
            }
            return result;
        }

        //非同期処理中に実行したい処理
        @Override
        public void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

        }

        //非同期処理完了時に実行したい処理
        @Override
        public void onPostExecute(String result) {


            if (_success) {
                String strImg = "";

                String productID = "";
                String productName = "";
                String makerID = "";
                String makerName = "";
                String summary = "";


                String jsonStr = "";
                onProgressUpdate(getString(R.string.msg_parse_before));

                try {
                    JSONArray rootJSON = new JSONArray(result);
                    //System.out.println("結果" + rootJSON);


                    for (int i = 0; i < rootJSON.length(); i++) {
                        JSONObject object = rootJSON.getJSONObject(i);
                        //Map<String, String> map = new HashMap<String, String>();
                        strImg = object.getString("img");
                      //  rackName = object.getString("rackName");
                        productID = object.getString("productID");
                        productName = object.getString("productName");
                        makerID = object.getString("maker");
                        makerName = object.getString("makerName");
                        summary = object.getString("productSummary");


//                        map.put("rackName", rackName);
//                        map.put("productID", productID);
//                        map.put("productName", productName);
//                        map.put("makerID", makerID);
//                        map.put("makerName", makerName);
//                        map.put("summary", summary);

                        //  listResult.add(map);

                    }



                    String img = strImg.replaceFirst("data:[^,]*,", "");

//                    Resources r = getResources();
//                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//
//                    Bitmap bmp = BitmapFactory.decodeResource(r, R.drawable.img);
//                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                    //byte[] test =  {0x68,0x65,0x6c,0x6c,0x6f};
                    //System.out.println("Base64" + Base64.encodeToString(test,Base64.DEFAULT));
                    //                    System.out.println(strImg);

                    byte[] bytes = Base64.decode(img,Base64.DEFAULT);

                   Bitmap bmpTest = (Bitmap) BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    productImg = (ImageView) findViewById(R.id.product_img);
                    productImg.setImageBitmap(bmpTest);


                    txRackName = (TextView) findViewById(R.id.rack_name);
                    txRackName.setText(rackName);

                    txProductID = (TextView) findViewById(R.id.product_id);
                    txProductID.setText(productID);

                    txProductName = (TextView) findViewById(R.id.product_name);
                    txProductName.setText(productName);

                    txMakerID = (TextView) findViewById(R.id.maker_id);
                    txMakerID.setText(makerID);

                    txMakerName = (TextView) findViewById(R.id.maker_name);
                    txMakerName.setText(makerName);

                    txSummary = (TextView) findViewById(R.id.summary);
                    txSummary.setText(summary);


                } catch (JSONException ex) {
                    onProgressUpdate(getString(R.string.msg_err_parse));
                    Log.e(DEBUG_TAG, "JSON解析失敗", ex);
                }

                onProgressUpdate(getString(R.string.msg_parse_after));


            }
        }


        /**
         * InputStreamオブジェクトを文字列に変換するメソッド
         * これをListに変換
         *
         * @param is
         * @return
         * @throws IOException
         */
        private String is2String(InputStream is) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            StringBuffer sb = new StringBuffer();
            char[] b = new char[1024];
            int line;
            while (0 <= (line = reader.read(b))) {
                sb.append(b, 0, line);
            }
            return sb.toString();
        }
    }


}
