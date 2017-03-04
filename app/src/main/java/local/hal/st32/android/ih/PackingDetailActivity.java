package local.hal.st32.android.ih;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

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

public class PackingDetailActivity extends SlideMenuItemActivity {

    GlobalsClass globals;
    private String URL = "PackingDetailServlet";
    //private static final String URL = "http://192.168.1.108:8080/IHAndroid/PackingDetailServlet";

    private String orderID;
    private ArrayList<String> detailList;
    private ArrayList<String> orderList;
    private String employeeID;
    ListView PackingDetail;
    List<Map<String, String>> listResult = new ArrayList<Map<String, String>>();
    String postalCode = "";
    String addressee = "";
    String address= "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packing_detail);

        globals = (GlobalsClass) this.getApplication();
        URL = globals.URL(URL);
        employeeID = globals.getEmployeeID();
        System.out.println("従業員ID" + employeeID);
        //ToolBarの設定
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Toolbarにtitle設定
        getSupportActionBar().setTitle("梱包詳細");

        //SlideMenuの設定
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.packing_detail_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        setDrawerLayout(R.id.packing_detail_drawer_layout);

        //NavigationViewのクリックリスナ
        NavigationView navigationView = (NavigationView) findViewById(R.id.packing_detail_nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        PackingDetail = (ListView) findViewById(R.id.packing_detail_list);

        //クリックリスナーをSET
        PackingDetail.setOnItemClickListener(new ListItemClickListener());

        Intent intent = getIntent();
        orderID = intent.getStringExtra("orderID");
        orderList = intent.getStringArrayListExtra("orderList");

        //非同期通信クラスを呼び出す
        RestAccess access = new RestAccess();
        access.execute(URL,orderID,"NG");

    }

    //Toolbar設定
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_packing_detail, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_finish:

                //検品する受注リストを格納するList
              //  ArrayList<String> detailList = new ArrayList<String>();
                detailList =  new ArrayList<String>();
                //ListView中のViewを一行ずつ取得
                for (int i = 0; i < PackingDetail.getChildCount(); i++) {
                    View view = (View) PackingDetail.getChildAt(i);
                    CheckBox checkState = (CheckBox) view.findViewById(R.id.checkState);

                    //チェックされた受注リストをListに格納
                    if (checkState.isChecked()) {
                        TextView txProductID = (TextView) view.findViewById(R.id.product_id);
                        detailList.add(txProductID.getText().toString());
                    }

                }

                //検品する受注リストを選択してなかったらアラートを表示
              //  if (detailList.size() != PackingDetail.getChildCount()) {
                if (detailList.size() != PackingDetail.getCount()) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(PackingDetailActivity.this);

                    builder.setTitle("未確認");
                    builder.setMessage("全商品を確認してください");

                    builder.setNegativeButton("OK", new PackingDetailActivity.DialogButtonClickListener());

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else
                {



                    AlertDialog.Builder show = new AlertDialog.Builder(PackingDetailActivity.this);

                    show.setTitle(orderID);
                    show.setMessage(postalCode + "\n" + addressee + "\n" + address);

                    show.setNegativeButton("確認済", new PackingDetailActivity.CheckFinishClickListener());
                    show.setCancelable(false);
                    AlertDialog dialog = show.create();
                    dialog.show();

                    RestAccess access = new RestAccess();
                    System.out.println("product_list***" + detailList.toString());
                    access.execute(URL,orderID,"OK",detailList.toString(),employeeID);




                }

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
            String postData = "";
            if (params.length == 5)
            {
                postData = "orderID=" + params[1] + "&flg=" + params[2] + "&productID=" + params[3] + "&employeeID=" + params[4];
            }
            else
            {
                postData = "orderID=" + params[1] + "&flg=" + params[2];
            }
            System.out.println("postData***" + postData);
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

        }

        //非同期処理完了時に実行したい処理
        @Override
        public void onPostExecute(String result) {


            if (_success && !result.isEmpty()) {
                String productID = "";
                String productName = "";
                String num = "";



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
                        num = object.getString("orderItemQuantity");

                        map.put("productID", productID);
                        map.put("productName", productName);
                        map.put("num", num);
                        System.out.println("mapの結果" + map);
                        listResult.add(map);

                        postalCode = object.getString("postalCode");
                        addressee =  object.getString("addressFirst") +  object.getString("addressEnd");
                        address=  object.getString("addressee");
                    }

                    String[] from = {"productID", "productName","num"};
                    int[] to = {R.id.product_id, R.id.product_name,R.id.num};
                    SimpleAdapter adapter = new SimpleAdapter(PackingDetailActivity.this, listResult, R.layout.custom_packing_detail, from, to);
                    PackingDetail.setAdapter(adapter);




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

    /**
     * ListViewがタップされた時の処理
     */
    public class ListItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


            CheckBox checkState = (CheckBox) view.findViewById(R.id.checkState);


            checkState.setChecked(!checkState.isChecked());


        }
    }


    /**
     * ダイアログ表示
     */
    public class DialogButtonClickListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:

                    break;
//                case DialogInterface.BUTTON_NEGATIVE:
//                    break;

            }
        }
    }


    public class CheckFinishClickListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    System.out.println("ダイアログ1");
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    System.out.println("ダイアログ2");
                    Intent intent = new Intent(PackingDetailActivity.this, PackingListActivity.class);
                    intent.putStringArrayListExtra("orderList",orderList);
                    System.out.println("受け取ったDETAILORDERLIST +***" + orderList);
                    startActivity(intent);
                    PackingDetailActivity.this.finish();
                   break;

            }
        }
    }

}
