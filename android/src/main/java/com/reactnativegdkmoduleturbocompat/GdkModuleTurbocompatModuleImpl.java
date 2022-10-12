package com.reactnativegdkmodule;

import android.util.Log;

import androidx.annotation.NonNull;

import com.blockstream.libgreenaddress.GdkModuleModuleImpl;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.module.annotations.ReactModule;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;

/**
 * This is where the module implementation lives
 * The exposed methods can be defined in the `turbo` and `legacy` folders
 */
public class GdkModuleModuleImpl {
  public static final String NAME = "GdkModule";

  protected HashMap<String, Object> sessions = new HashMap<>();
  protected JSONConverterImpl JSONConverter;

  private void debug(String message) {
    Log.d(NAME, message);
  }

  private void handleReject(Promise promise, String code, String message) {
    this.debug(String.format(Locale.US, "ERROR: %s, CAUSE: %s", code, message));
    promise.reject(String.format(Locale.US, "%s_%s_ERROR", NAME, code), message);
  }

  private static String randomString(int length) {
    String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    String randString = "";
    for (int i = 0; i < length; i++) {
      int randomIndex = (int) Math.floor(Math.random() * letters.length() + 1);
      String randomChar = Character.toString(letters.charAt(randomIndex));
      randString += randomChar;
    }
    return randString;
  }

  private void sessionGuard(String sessionId) throws Exception {
    if (!this.sessions.containsKey(sessionId)) {
      throw new Exception("SESSION_ERROR: Session doesn't exist.");
    }
  }

  private void sendEvent(String eventName,
                         WritableMap params) {
    this.getReactApplicationContext()
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
      .emit(eventName, params);
  }

  private JSONObject dummyResolve(GdkTwoFactorCall call) throws Exception {
    while (true) {
      JSONObject json = call.getStatus();
      String status = json.getString("status");

      if (status.equals("call")) {
        call.call();
      } else if (status.equals("done")) {
        return json;
      } else if (status.equals("error")) {
        throw new Exception(json.getString("error"));
      }
    }
  }

  public static void addListener(String eventName) {
    // Set up any upstream listeners or background tasks as necessary
  }

  public static void removeListeners(Integer count) {
    // Remove upstream listeners, stop unnecessary background tasks
  }

  public static void generateMnemonic12(Promise promise) {
    this.debug(String.format(Locale.US, "generateMnemonic12()"));
    promise.resolve(GdkModuleModuleImpl.generate_mnemonic_12());
  }

  public static void validateMnemonic(String mnemonic, Promise promise) {
    this.debug(String.format(Locale.US, "validateMnemonic(%s)", mnemonic));
    try {
      promise.resolve(GdkModuleModuleImpl.validate_mnemonic(mnemonic, 0));
    } catch (Exception e) {
      this.handleReject(promise, "VALIDATE_MNEMONIC", e.getMessage());
    }
  }

  public static void gdkInit(Promise promise) {
    this.debug(String.format(Locale.US, "gdkInit()"));
    try {
      File rootDataDir = getReactApplicationContext().getFilesDir();
      this.JSONConverter = new JSONConverterImpl();
      JSONObject config = new JSONObject();
      config.put("datadir", rootDataDir.getAbsolutePath());
      GdkModuleModuleImpl.init(this.JSONConverter, config);
      GdkModuleModuleImpl.setNotificationHandler((session, jsonObject) -> {
        try {
          JSONObject notification = new JSONObject(jsonObject.toString());
          if (!notification.isNull("event")) {
            sendEvent(notification.getString("event"), ReactNativeJson.convertJsonToMap(notification));
          }
        } catch (Exception e) {
          WritableMap params = Arguments.createMap();
          params.putString("error", e.getMessage());
          sendEvent("error", params);
        }
      });
      promise.resolve(true);
    } catch (Exception e) {
      this.handleReject(promise, "INIT", e.getMessage());
    }
  }

  public static void createSession(Promise promise) {
    this.debug(String.format(Locale.US, "createSession()"));
    try {
      String id = randomString(8);
      this.sessions.put(id, GdkModuleModuleImpl.create_session());
      promise.resolve(id);
    } catch (Exception e) {
      this.handleReject(promise, "CREATE_SESSION", e.getMessage());
    }
  }

  public static void connect(String sessionId, ReadableMap netParams, Promise promise) {
    this.debug(String.format(Locale.US, "connect(%s, %s)", sessionId, netParams.toString()));
    try {
      sessionGuard(sessionId);
      GdkModuleModuleImpl.connect(this.sessions.get(sessionId), ReactNativeJson.convertMapToJson(netParams));
      // TODO send event
      promise.resolve(true);
    } catch (Exception e) {
      this.handleReject(promise, "CONNECT", e.getMessage());
    }
  }

  public static void login(String sessionId, String mnemonic, Promise promise) {
    this.debug(String.format(Locale.US, "login(%s, %s)", sessionId, mnemonic));
    try {
      sessionGuard(sessionId);
      JSONObject params = new JSONObject();
      params.put("mnemonic", mnemonic);
      params.put("password", "");
      GdkTwoFactorCall call = new GdkTwoFactorCall(GdkModuleModuleImpl.login_user(this.sessions.get(sessionId), new JSONObject(), params));
      dummyResolve(call);
      promise.resolve(true);
    } catch (Exception e) {
      this.handleReject(promise, "LOGIN", e.getMessage());
    }
  }

  public static void getReceiveAddress(String sessionId, ReadableMap details, Promise promise) {
    this.debug(String.format(Locale.US, "getReceiveAddress(%s, %s)", sessionId, details.toString()));
    try {
      sessionGuard(sessionId);
      GdkTwoFactorCall call = new GdkTwoFactorCall(GdkModuleModuleImpl.get_receive_address(this.sessions.get(sessionId), ReactNativeJson.convertMapToJson(details)));
      JSONObject json = dummyResolve(call);
      promise.resolve(json.getJSONObject("result").getString("address"));
    } catch (Exception e) {
      this.handleReject(promise, "GET_RECEIVE_ADDRESS", e.getMessage());
    }
  }

  public static void getNetworks(Promise promise) {
    this.debug(String.format(Locale.US, "getNetworks()"));
    try {
      promise.resolve(ReactNativeJson.convertJsonToMap(new JSONObject(GdkModuleModuleImpl.get_networks().toString())));
    } catch (Exception e) {
      this.handleReject(promise, "GET_NETWORKS", e.getMessage());
    }
  }

  public static void registerNetwork(String name, ReadableMap details, Promise promise) {
    this.debug(String.format(Locale.US, "registerNetwork(%s, %s)", name, details.toString()));
    try {
      GdkModuleModuleImpl.register_network(name, ReactNativeJson.convertMapToJson(details));
      promise.resolve(true);
    } catch (Exception e) {
      this.handleReject(promise, "REGISTER_NETWORK", e.getMessage());
    }
  }

  public static void getTransactions(String sessionId, ReadableMap details, Promise promise) {
    this.debug(String.format(Locale.US, "getTransactions(%s, %s)", sessionId, details.toString()));
    try {
      sessionGuard(sessionId);
      GdkTwoFactorCall call = new GdkTwoFactorCall(GdkModuleModuleImpl.get_transactions(this.sessions.get(sessionId), ReactNativeJson.convertMapToJson(details)));
      JSONObject json = dummyResolve(call);
      if (!json.isNull("error") && !json.getString("error").equals("")) {
        throw new Exception(json.getString("error"));
      }
      WritableArray out = ReactNativeJson.convertJsonToArray(json.getJSONObject("result").getJSONArray("transactions"));
      promise.resolve(out);
    } catch (Exception e) {
      this.handleReject(promise, "GET_TRANSACTIONS", e.getMessage());
    }
  }

  public static void refresh(String sessionId, Promise promise) {
    this.debug(String.format(Locale.US, "refresh(%s)", sessionId));
    try {
      sessionGuard(sessionId);
      JSONObject params = new JSONObject();
      params.put("icons", true);
      params.put("assets", true);
      params.put("refresh", true);
      WritableMap out = ReactNativeJson.convertJsonToMap(new JSONObject(GdkModuleModuleImpl.refresh_assets(this.sessions.get(sessionId), params).toString()));
      promise.resolve(out);
    } catch (Exception e) {
      this.handleReject(promise, "REFRESH", e.getMessage());
    }
  }

  public static void getFeeEstimates(String sessionId, Promise promise) {
    this.debug(String.format(Locale.US, "getFeeEstimates(%s)", sessionId));
    try {
      sessionGuard(sessionId);
      JSONObject json = new JSONObject(GdkModuleModuleImpl.get_fee_estimates(this.sessions.get(sessionId)).toString());
      if (!json.isNull("error") && !json.getString("error").equals("")) {
        throw new Exception(json.getString("error"));
      }
      JSONArray fees = json.getJSONArray("fees");
      JSONObject out = new JSONObject();
      out.put("default", fees.isNull(fees.length() - 1) ? fees.getInt(-1) : 1000);
      out.put("fast", fees.isNull(1) ? fees.getInt(1) : 1000);
      promise.resolve(ReactNativeJson.convertJsonToMap(out));
    } catch (Exception e) {
      this.handleReject(promise, "GET_FEE_ESTIMATES", e.getMessage());
    }
  }

  public static void getBalance(String sessionId, ReadableMap details, Promise promise) {
    this.debug(String.format(Locale.US, "getBalance(%s, %s)", sessionId, details.toString()));
    try {
      sessionGuard(sessionId);
      GdkTwoFactorCall call = new GdkTwoFactorCall(GdkModuleModuleImpl.get_balance(this.sessions.get(sessionId), ReactNativeJson.convertMapToJson(details)));
      JSONObject json = dummyResolve(call);
      if (!json.isNull("error") && !json.getString("error").equals("")) {
        throw new Exception(json.getString("error"));
      }
      promise.resolve(ReactNativeJson.convertJsonToMap(json.getJSONObject("result")));
    } catch (Exception e) {
      this.handleReject(promise, "GET_BALANCE", e.getMessage());
    }
  }

  public static void getSubaccounts(String sessionId, ReadableMap details, Promise promise) {
    this.debug(String.format(Locale.US, "getSubaccounts(%s, %s)", sessionId, details.toString()));
    try {
      sessionGuard(sessionId);
      GdkTwoFactorCall call = new GdkTwoFactorCall(GdkModuleModuleImpl.get_subaccounts(this.sessions.get(sessionId), ReactNativeJson.convertMapToJson(details)));
      JSONObject json = dummyResolve(call).getJSONObject("result");
      if (!json.isNull("error")) {
        throw new Exception(json.getString("error"));
      } else {
        promise.resolve(ReactNativeJson.convertJsonToArray(json.getJSONArray("subaccounts")));
      }
    } catch (Exception e) {
      this.handleReject(promise, "GET_SUBACCOUNTS", e.getMessage());
    }
  }

  public static void createTransaction(String sessionId, ReadableMap details, Promise promise) {
    this.debug(String.format(Locale.US, "createTransaction(%s, %s)", sessionId, details.toString()));
    try {
      sessionGuard(sessionId);
      GdkTwoFactorCall call = new GdkTwoFactorCall(GdkModuleModuleImpl.create_transaction(this.sessions.get(sessionId), ReactNativeJson.convertMapToJson(details)));
      JSONObject json = dummyResolve(call).getJSONObject("result");
      if (!json.isNull("error") && !json.getString("error").equals("")) {
        throw new Exception(json.getString("error"));
      }
      promise.resolve(ReactNativeJson.convertJsonToMap(json));
    } catch (Exception e) {
      this.handleReject(promise, "CREATE_TRANSACTION", e.getMessage());
    }
  }

  public static void sendTransaction(String sessionId, ReadableMap details, Promise promise) {
    this.debug(String.format(Locale.US, "sendTransaction(%s, %s)", sessionId, details.toString()));
    try {
      sessionGuard(sessionId);
      GdkTwoFactorCall sendTransactionCall = new GdkTwoFactorCall(GdkModuleModuleImpl.send_transaction(this.sessions.get(sessionId), ReactNativeJson.convertMapToJson(details)));
      JSONObject json = dummyResolve(sendTransactionCall).getJSONObject("result");
      if (!json.isNull("error") && !json.getString("error").equals("")) {
        throw new Exception(json.getString("error"));
      }
      promise.resolve(ReactNativeJson.convertJsonToMap(json));
    } catch (Exception e) {
      this.handleReject(promise, "SEND_TRANSACTION", e.getMessage());
    }
  }

  public static void getUnspentOutputs(String sessionId, ReadableMap details, Promise promise) {
    this.debug(String.format(Locale.US, "getUnspentOutputs(%s, %s)", sessionId, details.toString()));
    try {
      sessionGuard(sessionId);
      GdkTwoFactorCall call = new GdkTwoFactorCall(GdkModuleModuleImpl.get_unspent_outputs(this.sessions.get(sessionId), ReactNativeJson.convertMapToJson(details)));
      JSONObject json = dummyResolve(call);
      promise.resolve(ReactNativeJson.convertJsonToMap(json.getJSONObject("result").getJSONObject("unspent_outputs")));
    } catch (Exception e) {
      this.handleReject(promise, "GET_UNSPENT_OUTPUTS", e.getMessage());
    }
  }

  public static void setPin(String sessionId, String mnemonic, String pin, Promise promise) {
    this.debug(String.format(Locale.US, "setPin(%s, %s, %s)", sessionId, mnemonic, pin));
    try {
      sessionGuard(sessionId);
      JSONObject json = new JSONObject(GdkModuleModuleImpl.set_pin(this.sessions.get(sessionId), mnemonic, pin, "").toString());
      promise.resolve(ReactNativeJson.convertJsonToMap(json));
    } catch (Exception e) {
      this.handleReject(promise, "SET_PIN", e.getMessage());
    }
  }

  public static void loginWithPin(String sessionId, String pin, ReadableMap pin_data, Promise promise) {
    this.debug(String.format(Locale.US, "loginWithPin(%s, %s, %s)", sessionId, pin, pin_data.toString()));
    try {
      sessionGuard(sessionId);
      JSONObject params = new JSONObject();
      params.put("pin", pin);
      params.put("pin_data", ReactNativeJson.convertMapToJson(pin_data));
      GdkTwoFactorCall call = new GdkTwoFactorCall(GdkModuleModuleImpl.login_user(this.sessions.get(sessionId), new JSONObject(), params));
      JSONObject json = dummyResolve(call);
      promise.resolve(true);
    } catch (Exception e) {
      this.handleReject(promise, "LOGIN_WITH_PIN", e.getMessage());
    }
  }

  public static void signTransaction(String sessionId, ReadableMap details, Promise promise) {
    this.debug(String.format(Locale.US, "signTransaction(%s, %s)", sessionId, details.toString()));
    try {
      sessionGuard(sessionId);
      GdkTwoFactorCall signTransactionCall = new GdkTwoFactorCall(GdkModuleModuleImpl.sign_transaction(this.sessions.get(sessionId), ReactNativeJson.convertMapToJson(details)));
      JSONObject signTransactionJson = dummyResolve(signTransactionCall).getJSONObject("result");
      if (!signTransactionJson.isNull("error") && !signTransactionJson.getString("error").equals("")) {
        throw new Exception(signTransactionJson.getString("error"));
      }
      promise.resolve(ReactNativeJson.convertJsonToMap(signTransactionJson));
    } catch (Exception e) {
      this.handleReject(promise, "SIGN_TRANSACTION", e.getMessage());
    }
  }

  public static void broadcastTransaction(String sessionId, String transactionHex, Promise promise) {
    this.debug(String.format(Locale.US, "broadcastTransaction(%s, %s)", sessionId, transactionHex));
    try {
      sessionGuard(sessionId);
      promise.resolve(GdkModuleModuleImpl.broadcast_transaction(this.sessions.get(sessionId), transactionHex));
    } catch (Exception e) {
      this.handleReject(promise, "BROADCAST_TRANSACTION", e.getMessage());
    }
  }

  public static void createSubaccount(String sessionId, ReadableMap details, Promise promise) {
    this.debug(String.format(Locale.US, "createSubaccount(%s, %s)", sessionId, details.toString()));
    try {
      sessionGuard(sessionId);
      GdkTwoFactorCall call = new GdkTwoFactorCall(GdkModuleModuleImpl.create_subaccount(this.sessions.get(sessionId), ReactNativeJson.convertMapToJson(details)));
      JSONObject json = dummyResolve(call).getJSONObject("result");
      if (!json.isNull("error")) {
        throw new Exception(json.getString("error"));
      } else {
        promise.resolve(ReactNativeJson.convertJsonToMap(json));
      }
    } catch (Exception e) {
      this.handleReject(promise, "CREATE_SUBACCOUNT", e.getMessage());
    }
  }

  public static void getSubaccount(String sessionId, double subaccount, Promise promise) {
    this.debug(String.format(Locale.US, "getSubaccount(%s, %df)", sessionId, subaccount));
    try {
      sessionGuard(sessionId);
      GdkTwoFactorCall call = new GdkTwoFactorCall(GdkModuleModuleImpl.get_subaccount(this.sessions.get(sessionId), (long) subaccount));
      JSONObject json = dummyResolve(call).getJSONObject("result");
      if (!json.isNull("error")) {
        throw new Exception(json.getString("error"));
      } else {
        promise.resolve(ReactNativeJson.convertJsonToMap(json));
      }
    } catch (Exception e) {
      this.handleReject(promise, "GET_SUBACCOUNT", e.getMessage());
    }
  }

  public static void getMnemonic(String sessionId, Promise promise) {
    this.debug(String.format(Locale.US, "getMnemonic(%s)", sessionId));
    try {
      sessionGuard(sessionId);
      promise.resolve(GdkModuleModuleImpl.get_mnemonic_passphrase(this.sessions.get(sessionId), ""));
    } catch (Exception e) {
      this.handleReject(promise, "GET_MNEMONIC", e.getMessage());
    }
  }

  public static void loginUser(String sessionId, ReadableMap hwDevice, ReadableMap details, Promise promise) {
    this.debug(String.format(Locale.US, "loginUser(%s, %s, %s)", sessionId, hwDevice.toString(), details.toString()));
    try {
      sessionGuard(sessionId);
      GdkTwoFactorCall call = new GdkTwoFactorCall(GdkModuleModuleImpl.login_user(this.sessions.get(sessionId), ReactNativeJson.convertMapToJson(hwDevice), ReactNativeJson.convertMapToJson(details)));
      dummyResolve(call);
      promise.resolve(true);
    } catch (Exception e) {
      this.handleReject(promise, "LOGIN_USER", e.getMessage());
    }
  }

  public static void registerUser(String sessionId, ReadableMap hwDevice, String mnemonic, Promise promise) {
    this.debug(String.format(Locale.US, "registerUser(%s, %s, %s)", sessionId, hwDevice, mnemonic));
    try {
      sessionGuard(sessionId);
      GdkTwoFactorCall call = new GdkTwoFactorCall(GdkModuleModuleImpl.register_user(this.sessions.get(sessionId), ReactNativeJson.convertMapToJson(hwDevice), mnemonic));
      dummyResolve(call);
      promise.resolve(true);
    } catch (Exception e) {
      this.handleReject(promise, "REGISTER_USER", e.getMessage());
    }
  }

  public static void getTransactionDetails(String sessionId, String txHash, Promise promise) {
    this.debug(String.format(Locale.US, "getTransactionDetails(%s, %s)", sessionId, txHash));
    try {
      sessionGuard(sessionId);
      GdkTwoFactorCall call = new GdkTwoFactorCall(GdkModuleModuleImpl.get_transaction_details(this.sessions.get(sessionId), txHash));
      JSONObject json = dummyResolve(call).getJSONObject("result");
      promise.resolve(ReactNativeJson.convertJsonToMap(json));
    } catch (Exception e) {
      this.handleReject(promise, "GET_TRANSACTION_DETAILS", e.getMessage());
    }
  }

  public static void destroySessions(Promise promise) {
    this.debug("destroySessions()");
    this.sessions.clear();
    promise.resolve(true);
  }
}
