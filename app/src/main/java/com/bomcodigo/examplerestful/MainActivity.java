package com.bomcodigo.examplerestful;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
    private TextView txtConteudo;
    private EditText edtPosicao;
    private Button btnConsultar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtConteudo = (TextView) findViewById(R.id.txtViewConteudo);
        edtPosicao = (EditText) findViewById(R.id.edtPosicao);
        btnConsultar = (Button) findViewById(R.id.btnConsultar);

        btnConsultar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String posicao = edtPosicao.getText().toString();
                if  (! TextUtils.isEmpty(posicao) && TextUtils.isDigitsOnly(edtPosicao.getText())) {
                    consultar(Integer.parseInt(posicao));
                }
            }
        });
    }

    private void consultar(int posicao){
        txtConteudo.setText("");
        new Get(this).execute(posicao);
    }

    private class Get extends AsyncTask<Integer, String, Veiculo>{
        private static final String TAG = "WEBSERVICE";
        private ProgressDialog progressDialog;
        private Context context;

        public Get(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            Log.d(TAG,"onPreExecute");
            progressDialog = new ProgressDialog(context);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle("Aguarde...");
            progressDialog.setMessage("Aguardando a resposta do servidor...");
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(false);
            progressDialog.show();
        }

        @Override
        protected Veiculo doInBackground(Integer... integers) {
            Veiculo retorno = null;
            try {
                String uri = String.format("http://bomcodigo.com/webservice/example/veiculo/%d",integers[0]);
                Log.d(TAG,uri);

                HttpClient httpClient = new DefaultHttpClient();
                HttpContext localContext = new BasicHttpContext();
                HttpGet httpGet = new HttpGet(uri);

                Log.d(TAG,"Realizando requisição.");
                HttpResponse response = httpClient.execute(httpGet, localContext);

                int statusCode = response.getStatusLine().getStatusCode();
                Log.d(TAG,"status: " + statusCode);
                if (statusCode == HttpStatus.SC_OK){
                    HttpEntity entity = response.getEntity();
                    InputStream input = entity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input, "utf8"));
                    StringBuilder sb = new StringBuilder();
                    String line = null;

                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    input.close();
                    String serverResponse = sb.toString();
                    Log.d(TAG,"serverResponse: " + serverResponse);
                    if (! TextUtils.isEmpty(serverResponse)) {
                        retorno = new Veiculo();

                        JSONObject jsonObject = new JSONObject(serverResponse);

                        retorno.setPosicao(jsonObject.getInt("posicao"));
                        retorno.setVeiculo(jsonObject.getString("veiculo"));
                        retorno.setQuantidade(jsonObject.getDouble("qtd"));
                    }
                }
            } catch (IOException e) {
                Log.d(TAG,"Erro na requisição: " + e.getMessage());
                e.printStackTrace();
            } catch (JSONException e) {
                Log.d(TAG,"Erro na requisição: " + e.getMessage());
                e.printStackTrace();
            }
            return retorno;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            progressDialog.setMessage(values[0]);
        }

        @Override
        protected void onPostExecute(Veiculo result)
        {
            super.onPostExecute(result);
            if  (result != null){
                txtConteudo.setText(String.format("O %dº veículo mais vendido em 2016 é o %s com %s quantidades vendidas.",
                                                    result.getPosicao(),
                                                    result.getVeiculo(),
                                                    result.getQuantidade()));
            }

            progressDialog.hide();
            progressDialog.dismiss();
        }
    }
}
