package com.inputstick.apps.kp2aplugin.keepassdx;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.inputstick.apps.kp2aplugin.Const;
import com.inputstick.apps.kp2aplugin.InputStickService;
import com.inputstick.apps.kp2aplugin.PreferencesHelper;

import org.keepassdx.output.IOutputCredentialsService;

public class OutputCredentialsService extends Service {

    private static final String TAG = "IS-OutputService";

    // Return codes 
    private static final int RC_OK           = 0;
    private static final int RC_UNKNOWN_MODE = -1;
    private static final int RC_EXCEPTION    = -2;
    private static final int RC_DISABLED     = -3;
    private static final int RC_EMPTY        = -8;

    // Adjust these to match your KeePassDX-kb package(s)
    private static final String[] ALLOWED_CALLER_PREFIXES = new String[] {
            "com.kunzisoft.keepass" // KeePassDX flavors share 
    };

    private final IOutputCredentialsService.Stub binder = new IOutputCredentialsService.Stub() {

        @Override
        public String getProviderName() {
            enforceKeePassDxCaller();
            return "InputStick";
        }

        @Override
        public int sendPayload(
                String requestId,
                String mode,
                String username,
                String password,
                String otp,
                String entryTitle,
                String entryUuid
        ) {
            enforceKeePassDxCaller();

            try {
                final String safeMode = (mode == null) ? "unknown" : mode;
                final String user = (username == null) ? "" : username;
                final String pass = (password == null) ? "" : password;
                final String otpVal = (otp == null) ? "" : otp;

                Log.d(TAG, "sendPayload id=" + requestId
                        + " mode=" + safeMode
                        + " title=" + entryTitle
                        + " uuid=" + entryUuid);

                // Build payload 
                switch (safeMode) {
                    case "user":
                        payload = user;
                        break;
                    case "pass":
                        payload = pass;
                        break;
                    case "user_tab_pass_enter":
                        payload = user + "\t" + pass + "\n";
                        break;
                    case "user_enter_pass_enter":
                        payload = user + "\n" + pass + "\n";
                        break;
                    default:
                        Log.w(TAG, "Unknown mode: " + safeMode);
                        return RC_UNKNOWN_MODE;
                }

                if (!otpVal.isEmpty()) {
                    payload = payload + otpVal;
                }

                if (payload.isEmpty()) {
                    Log.w(TAG, "Empty payload");
                    return RC_EMPTY;
                }

                // Route into the SAME execution path the plugin uses: start InputStickService with extras.
                // uses SERVICE_ENTRY_ACTION + EXTRA_ACTION routing
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(OutputCredentialsService.this);
                String layout = PreferencesHelper.getPrimaryLayoutCode(prefs);

                Intent i = new Intent(OutputCredentialsService.this, InputStickService.class);
                i.setAction(Const.SERVICE_ENTRY_ACTION);
                i.putExtra(Const.EXTRA_ACTION, Const.ACTION_DIRECT_TEXT); 
                i.putExtra(Const.EXTRA_LAYOUT, layout);
                i.putExtra(Const.EXTRA_TEXT, payload);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(i);
                } else {
                    startService(i);
                }

                return RC_OK;

            } catch (Throwable t) {
                Log.w(TAG, "sendPayload exception", t);
                return RC_EXCEPTION;
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind action=" + (intent != null ? intent.getAction() : "null"));
        return binder;
    }

    private void enforceKeePassDxCaller() {
        int callingUid = Binder.getCallingUid();
        String[] pkgs = getPackageManager().getPackagesForUid(callingUid);
        if (pkgs == null) pkgs = new String[0];

        for (String p : pkgs) {
            for (String prefix : ALLOWED_CALLER_PREFIXES) {
                if (p != null && p.startsWith(prefix)) return;
            }
        }

        Log.w(TAG, "Reject caller uid=" + callingUid);
        throw new SecurityException("Caller is not KeePassDX");
    }
}
