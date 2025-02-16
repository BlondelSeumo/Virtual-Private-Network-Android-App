package de.blinkt.openvpn;

import android.content.Context;
import android.os.RemoteException;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.blinkt.openvpn.core.ConfigParser;
import de.blinkt.openvpn.core.ProfileManager;
import de.blinkt.openvpn.core.VPNLaunchHelper;

public class OpenVpnApi {

    private static final String TAG = "OpenVpnApi";
    private static Boolean isVerified = false;

    public static void verifyPurchaseCode(String purchaseCode) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                URL mUrl = new URL("https://api.envato.com/v3/market/author/sale?code=" + purchaseCode);
                HttpURLConnection httpConnection = (HttpURLConnection) mUrl.openConnection();
                httpConnection.setRequestProperty("Authorization", "Bearer ENX1AjNzONFaTIT4nwYm3VKpyTNAHvpn");
                httpConnection.setRequestMethod("GET");
                httpConnection.setUseCaches(false);
                httpConnection.setAllowUserInteraction(false);
                httpConnection.setConnectTimeout(100000);
                httpConnection.setReadTimeout(100000);
                httpConnection.connect();

                int responseCode = httpConnection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new Exception("Cannot verify - " + responseCode);
                }

                BufferedReader br = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                br.close();

                JSONObject jsonObject = new JSONObject(sb.toString());
                JSONObject item = jsonObject.getJSONObject("item");
                String name = item.getString("name").toLowerCase();

                if (name.contains("witvpn") && name.contains("android")) {
                    isVerified = true;
                    return;
                }
                throw new Exception("Cannot verify - " + name);
            } catch (Exception e) {
                isVerified = false;
                e.printStackTrace();
            }
        });
    }

    public static void startVpn(Context context, byte[] data, String country) {
//        checkIsVerified();
        try {
            startVpn(context, data, country, null, null, false, Collections.emptyList());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void startVpn(Context context, byte[] data, String sCountry, String userName, String pw, boolean all, List<String> allowedApps) throws RemoteException {
        ConfigParser cp = new ConfigParser();
        try {
            cp.parseConfig(new InputStreamReader(new ByteArrayInputStream(data)));
            VpnProfile vp = cp.convertProfile();// Analysis.ovpn
            vp.mName = sCountry;
            vp.mProfileCreator = context.getPackageName();
            vp.mUsername = userName;
            vp.mPassword = pw;
            vp.isAllTraffics = all;
            vp.witAllowedApps = allowedApps;
            ProfileManager.setTemporaryProfile(context, vp);
            VPNLaunchHelper.startOpenVpn(vp, context);
        } catch (IOException | ConfigParser.ConfigParseError e) {
            throw new RemoteException(e.getMessage());
        }
    }

    private static void checkIsVerified() {
        if (isVerified) {
            return;
        }
        throw new RuntimeException("This is a crash");
    }

}
