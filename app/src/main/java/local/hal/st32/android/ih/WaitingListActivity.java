package local.hal.st32.android.ih;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
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
import java.util.Objects;

public class WaitingListActivity extends SlideMenuItemActivity {

    GlobalsClass globals;
    //アクセスするAPIのURL
   // private static final String URL = "http://192.168.1.108:8080/IHAndroid/WaitingServlet";
    private String URL = "WaitingServlet";
    private String repositoryID;

    //ListView
    ListView WaitingList;

    //カスタマイズListViewを表示するList
    List<Map<String, String>> listResult = new ArrayList<Map<String, String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_list);

        globals = (GlobalsClass) this.getApplication();
        URL = globals.URL(URL);
        repositoryID = globals.getRepositoryID();

        //ToolBarの設定
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Toolbarにtitle設定
        getSupportActionBar().setTitle("検品待ち一覧");

        //SlideMenuの設定
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.waiting_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        setDrawerLayout(R.id.waiting_drawer_layout);

        //NavigationViewのクリックリスナ
        NavigationView navigationView = (NavigationView) findViewById(R.id.waiting_nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //ListViewを取得
        WaitingList = (ListView) findViewById(R.id.waiting_list);

        //クリックリスナーをSET
        WaitingList.setOnItemClickListener(new ListItemClickListener());

        //非同期通信クラスを呼び出す
        RestAccess access = new RestAccess();
        access.execute(URL,repositoryID);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_waiting, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_next:

                //検品する受注リストを格納するList
                ArrayList<String> orderList = new ArrayList<String>();

                //ListView中のViewを一行ずつ取得
                for (int i = 0; i < WaitingList.getChildCount(); i++) {
                    View view = (View) WaitingList.getChildAt(i);
                    CheckBox checkState = (CheckBox) view.findViewById(R.id.checkState);

                    //チェックされた受注リストをListに格納
                    if (checkState.isChecked()) {
                        TextView txOrderID = (TextView) view.findViewById(R.id.orderID);
                        orderList.add(txOrderID.getText().toString());
                    }

                }

                //検品する受注リストを選択してなかったらアラートを表示
                if (orderList.size() == 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(WaitingListActivity.this);

                    builder.setTitle("未選択");
                    builder.setMessage("検品する受注リストを選択してください");

                    builder.setNegativeButton("OK", new DialogButtonClickListener());

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else
                {
                    Intent intent = new Intent(WaitingListActivity.this, ProductListActivity.class);
                    intent.putStringArrayListExtra("orderList",orderList);
                    startActivity(intent);
                    finish();
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

            String postData = "repositoryID=" + repositoryID;
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


            if (_success) {

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
                    SimpleAdapter adapter = new SimpleAdapter(WaitingListActivity.this, listResult, R.layout.custom_waiting_list, from, to);
                    WaitingList.setAdapter(adapter);


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



}
