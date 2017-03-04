package local.hal.st32.android.ih;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;


import android.content.Intent;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import net.arnx.jsonic.JSON;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductListActivity extends SlideMenuItemActivity {

    //アクセスするWebAPIのアドレス
    GlobalsClass globals;
    private String URL = "ProductListServlet";
   // private static final String URL = "http://192.168.1.108:8080/IHAndroid/ProductListServlet";

    //テスト用

    ListView productList;
    List<Map<String, String>> listResult = new ArrayList<Map<String, String>>();
    private ArrayList<String> orderList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        globals = (GlobalsClass) this.getApplication();
        URL = globals.URL(URL);
        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Toolbarにtitle設定
        getSupportActionBar().setTitle("検品商品一覧");

        //ListView product_list = (ListView) findViewById(R.id.product_list);

        //SlideMenu設定
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.product_list_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //SlideMenuのItem情報表示
        setDrawerLayout(R.id.product_list_drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.product_list_nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        productList = (ListView) findViewById(R.id.product_list);
        productList.setOnItemClickListener(new ListItemClickListener());

        Intent intent = getIntent();
        orderList = intent.getStringArrayListExtra("orderList");

        System.out.println("orderlist" + orderList.toString());
        RestAccess access = new RestAccess();
        access.execute(URL, orderList.toString());


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.menu_product_detail, menu);
        getMenuInflater().inflate(R.menu.menu_waiting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int itemId = item.getItemId();


        //検品する受注リストを格納するList
        ArrayList<String> list = new ArrayList<String>();

        //ListView中のViewを一行ずつ取得
        for (int i = 0; i < productList.getChildCount(); i++) {
            View view = (View) productList.getChildAt(i);
            CheckBox checkState = (CheckBox) view.findViewById(R.id.checkState);

            //チェックされた受注リストをListに格納
            if (checkState.isChecked()) {
                TextView txProductID = (TextView) view.findViewById(R.id.product_id);
                list.add(txProductID.getText().toString());
            }

        }
        System.out.println("listCount" + productList.getChildCount());
        System.out.println("listCount" + list.size());


        //検品する受注リストを選択してなかったらアラートを表示
        if (list.size() != productList.getChildCount()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ProductListActivity.this);

            builder.setTitle("検品");
            builder.setMessage("未検品があります");

            builder.setNegativeButton("OK", new ProductListActivity.DialogButtonClickListener());

            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else
        {
            Intent intent = new Intent(ProductListActivity.this, PackingListActivity.class);
            intent.putStringArrayListExtra("orderList",orderList);
            startActivity(intent);
            finish();
        }












        return super.onOptionsItemSelected(item);
    }


    /**
     * 検品商品一覧の完了ボタン
     * 梱包一覧に遷移
     *
     * @param
     */


    private class RestAccess extends AsyncTask<String, String, String> {
        private static final String DEBUG_TAG = "RestAccess";

        //private TextView _tvProcess;
        // private ListView list;
        private boolean _success = false;


//        public RestAccess(ListView list) {
//            list = productList;
//        }

        //非同期で実行したい処理
        @Override
        public String doInBackground(String... params) {
            String urlStr = params[0];
            HttpURLConnection con = null;
            InputStream is = null;
            String result = "";

            String postData = "orderList=" + params[1];
            System.out.println("postData" + postData);

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
                System.out.println("result結果" + result);
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
//            String message = _tvProcess.getText().toString();
//            if (!message.equals(""))
//            {
//                message += "\n";
//            }
//
//            message += values[0];
//            _tvProcess.setText(message);
        }

        //非同期処理完了時に実行したい処理
        @Override
        public void onPostExecute(String result) {


            if (_success) {

                String productID;
                String productName;
                String num;
                String rackName;


                String jsonStr = "";
                onProgressUpdate(getString(R.string.msg_parse_before));

                try {
                    JSONArray rootJSON = new JSONArray(result);
                    //System.out.println("結果" + rootJSON);


                    for (int i = 0; i < rootJSON.length(); i++) {

                        JSONObject object = rootJSON.getJSONObject(i);
                        Map<String, String> map = new HashMap<String, String>();
                        productID = object.getString("productID");
                        productName = object.getString("productName");
                        num = object.getString("productAllNum");
                        rackName = object.getString("rackName");

                        map.put("productID", productID);
                        map.put("productName", productName);
                        map.put("num", num);
                        map.put("rackName", rackName);
                        System.out.println("mapの結果" + map);
                        listResult.add(map);
                        System.out.println(listResult + "list");



                    }


                    String[] from = {"productID", "productName", "num", "rackName"};
                    int[] to = {R.id.product_id, R.id.product_name, R.id.num, R.id.rack_name};
                    SimpleAdapter adapter = new SimpleAdapter(ProductListActivity.this, listResult, R.layout.custom_product_list, from, to);
                    productList.setAdapter(adapter);


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


    private class DialogButtonClickListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {

        }
    }

    public class ListItemClickListener implements AdapterView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> parent,View view,int position,long id)
        {
            CheckBox checkState = (CheckBox) view.findViewById(R.id.checkState);

            checkState.setChecked(!checkState.isChecked());
        }
    }



    public void onInfoClick(View view)
    {
//        Intent intent = new Intent(ProductListActivity.this, ProductDetailActivity.class);
//        //intent.putExtra("mode", MODE_INSERT);
//        startActivity(intent);
     //   TextView txProudctID = (TextView) view.findViewById(R.id.product_id);
       // String productID = txProudctID.getText().toString();
        LinearLayout linearLayout = (LinearLayout) view.getParent();
        TextView txProudctID = (TextView) linearLayout.findViewById(R.id.product_id);
        TextView txRackName = (TextView) linearLayout.findViewById(R.id.rack_name);
        String rackName = txRackName.getText().toString();
        String productID = txProudctID.getText().toString();
        Intent intent = new Intent(ProductListActivity.this, ProductDetailActivity.class);
        intent.putExtra("productID", productID);
        intent.putExtra("rackName",rackName);
        startActivity(intent);
    }



}

