package local.hal.st32.android.ih;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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
import java.util.HashMap;
import java.util.Map;

public class EmployeeLoginActivity extends AppCompatActivity {

    GlobalsClass globals;
    private String URL = "LoginServlet";
   // private static final String URL = "http://192.168.1.108:8080/IHAndroid/LoginServlet";

    private String employeeID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_login);

        getSupportActionBar().setTitle("従業員ログイン");
        globals = (GlobalsClass) this.getApplication();
        URL = globals.URL(URL);

    }

    /**
     * 従業員IDとパスワード取得し
     * 非同期通信でログインチェック
     *
     * @param view
     */
    public void login(View view) {


        //従業員ID取得
        EditText edEmployeeID = (EditText) findViewById(R.id.employee_id);
        employeeID = edEmployeeID.getText().toString();


        //従業員パスワード取得
        EditText edPassWord = (EditText) findViewById(R.id.password);
        String password = edPassWord.getText().toString();

        System.out.println("従業員入力情報" + employeeID + password);

        RestAccess access = new RestAccess();

        access.execute(URL, employeeID, password);
    }


    private class RestAccess extends AsyncTask<String, String, String> {
        private static final String DEBUG_TAG = "RestAccess";

        private boolean _success = false;


//        public RestAccess(ListView list) {
//            list = productList;
//        }

        //非同期で実行したい処理
        @Override
        public String doInBackground(String... params) {
            String urlStr = params[0];
            String employeeID = params[1];
            String password = params[2];

            String postData = "employeeID=" + employeeID +
                    "&password=" + password;

            HttpURLConnection con = null;
            InputStream is = null;
            String result = "";

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
                String status = "";
                String mes = "";


                String jsonStr = "";
                onProgressUpdate(getString(R.string.msg_parse_before));

                try {
                    //JSONArray rootJSON = new JSONArray(result);
                    //System.out.println("結果" + rootJSON);


//                    for (int i = 0; i < rootJSON.length(); i++) {
                    JSONObject rootJSON = new JSONObject(result);
                        status = rootJSON.getString("status");
                        mes = rootJSON.getString("mes");


  //                  }

                    if (!status.equals("1"))
                    {
                        TextView txMes = (TextView) findViewById(R.id.mes);
                        txMes.setText(mes);
                    }
                    else
                    {
                        globals.setEmployeeID(employeeID);
                        System.out.println("従業員ID" + globals.getEmployeeID());
                        Intent intent = new Intent(EmployeeLoginActivity.this, WaitingListActivity.class);

                        startActivity(intent);

                        finish();
                    }


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
