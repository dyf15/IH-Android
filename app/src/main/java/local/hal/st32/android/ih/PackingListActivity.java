package local.hal.st32.android.ih;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
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

public class PackingListActivity extends SlideMenuItemActivity {
    GlobalsClass globals;
    private String URL = "PackingServlet";
   // private static final String URL = "http://192.168.1.108:8080/IHAndroid/PackingServlet";

    ListView PackingList;
    List<Map<String, String>> listResult = new ArrayList<Map<String, String>>();

    private ArrayList<String> orderList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packing_list);

        globals = (GlobalsClass) this.getApplication();
        URL = globals.URL(URL);
        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Toolbarにtitle設定
        getSupportActionBar().setTitle("梱包一覧");

        //ListView product_list = (ListView) findViewById(R.id.packing_list);

        //SlideMenu設定
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.packing_list_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        setDrawerLayout(R.id.packing_list_drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.packing_list_nav_view);
        navigationView.setNavigationItemSelectedListener(this);


         PackingList = (ListView) findViewById(R.id.packing_list);

        //クリックリスナーをSET
        PackingList.setOnItemClickListener(new ListItemClickListener());


        Intent intent = getIntent();
        orderList = intent.getStringArrayListExtra("orderList");


        System.out.println("受け取ったORDERLIST +***" + orderList);

        //非同期通信クラスを呼び出す
        RestAccess access = new RestAccess();
        access.execute(URL,orderList.toString());
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
            String postData = "orderList=" + params[1];

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

                String orderID;
                String addressee;

                String jsonStr = "";
                onProgressUpdate(getString(R.string.msg_parse_before));

                try {
                    JSONArray rootJSON = new JSONArray(result);
                    //System.out.println("結果" + rootJSON);


                    for (int i = 0; i < rootJSON.length(); i++) {
                        JSONObject object = rootJSON.getJSONObject(i);
                        Map<String, String> map = new HashMap<String, String>();
                        orderID = object.getString("orderID");
                        addressee = object.getString("addressee");
                        jsonStr += orderID + "\n" + addressee + "\n";
                        map.put("orderID", orderID);
                        map.put("addressee", addressee);
                        System.out.println("mapの結果" + map);
                        listResult.add(map);
                        System.out.println(listResult + "list");

                    }

                    String[] from = {"orderID", "addressee"};
                    int[] to = {R.id.orderID, R.id.addressee};
                    SimpleAdapter adapter = new SimpleAdapter(PackingListActivity.this, listResult, R.layout.custom_packing_list, from, to);
                    PackingList.setAdapter(adapter);


                } catch (JSONException ex) {
                    onProgressUpdate(getString(R.string.msg_err_parse));
                    Log.e(DEBUG_TAG, "JSON解析失敗", ex);
                }

                onProgressUpdate(getString(R.string.msg_parse_after));


            }
            if (PackingList.getCount() == 0)
            {

                AlertDialog.Builder show = new AlertDialog.Builder(PackingListActivity.this);

                show.setTitle("梱包完了");
                show.setMessage("梱包完了、お疲れ様でした。");
                show.setPositiveButton("ログアウト",new CheckFinishClickListener());
                show.setNegativeButton("引き続き検品", new CheckFinishClickListener());
                show.setCancelable(false);
                AlertDialog dialog = show.create();
                dialog.show();

//                setContentView(R.layout.packking_finish);
//                Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//                setSupportActionBar(toolbar);


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


            TextView tvOrderID = (TextView) findViewById(R.id.orderID);
            String orderID = tvOrderID.getText().toString();
            Intent intent = new Intent(PackingListActivity.this, PackingDetailActivity.class);
            intent.putExtra("orderID",orderID);
            intent.putStringArrayListExtra("orderList",orderList);
            startActivity(intent);


        }
    }

    private String isNull (String value)
    {
        if (value == null)
        {
            return "";
        }
        return value;
    }



    public class CheckFinishClickListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:

                    Intent intents = new Intent(PackingListActivity.this, EmployeeLoginActivity.class);
                    globals.setEmployeeID("");
                    startActivity(intents);
                    PackingListActivity.this.finish();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    System.out.println("引き続き検品");
                    Intent intent = new Intent(PackingListActivity.this, WaitingListActivity.class);

                    startActivity(intent);
                    PackingListActivity.this.finish();

                    break;

            }
        }
    }
}
