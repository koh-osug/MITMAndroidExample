/*
 * Copyright 2021 Karsten Ohme
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.ohmesoftware.mitmandroidexample;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.HttpProxyServerBootstrap;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.littleshoot.proxy.mitm.Authority;
import org.littleshoot.proxy.mitm.CertificateSniffingMitmManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;

public class MainActivity extends AppCompatActivity {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainActivity.class);

    private HttpProxyServer server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private void showError(Exception e) {
        runOnUiThread(() -> {
            String errorMsg = e.getMessage();
            LOGGER.error("Error", e);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
            final AlertDialog alertDialog = alertDialogBuilder
                    .setTitle(R.string.exception)
                    .setMessage(errorMsg)
                    .setPositiveButton(android.R.string.ok, (dialog, id) -> {
                        dialog.dismiss();
                    }).
                            setCancelable(false).create();

            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        });
    }

    private void showOk(String message) {
        runOnUiThread(() -> {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
            final AlertDialog alertDialog = alertDialogBuilder
                    .setTitle(R.string.success)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok, (dialog, id) -> {
                        dialog.dismiss();
                    }).
                            setCancelable(false).create();

            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        });
    }

    public void runProxy(View view) {
        new Thread(() -> {
            try {
                Authority authority = new Authority().withKeyStoreDir(getFilesDir());
                File p12 = authority.aliasFile(".p12");
                if (!p12.exists()) {
                    Files.copy(getAssets().open("littleproxy-mitm.p12"), p12.toPath());
                }
                File pem = authority.aliasFile(".pem");
                if (!pem.exists()) {
                    Files.copy(getAssets().open("littleproxy-mitm.pem"), pem.toPath());
                }

                KeyStore clientKeyStore = KeyStore.getInstance("PKCS12");
                clientKeyStore.load(getAssets().open("badssl.com-client.p12"),
                        "badssl.com".toCharArray());
                KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(clientKeyStore, "password".toCharArray());

                HttpProxyServerBootstrap bootstrap = DefaultHttpProxyServer
                        .bootstrapFromFile("./littleproxy.properties")
                        .withPort(9092).withAllowLocalOnly(true);

                bootstrap.withManInTheMiddle(new CertificateSniffingMitmManager(authority, kmf, null));
                server = bootstrap.start();
                runOnUiThread(() -> showOk(getString(R.string.success_msg)));
            } catch (Exception e) {
                runOnUiThread(() -> showError(e));
            }
        }).start();
    }

    public void stopProxy(View view) {
        new Thread(() -> {
            try {
                stopServer();
                runOnUiThread(() -> showOk(getString(R.string.success_stopped_msg)));
            } catch (Exception e) {
                runOnUiThread(() -> showError(e));
            }
        }).start();
    }

    private void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopServer();
    }
}