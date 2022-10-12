package com.reactnativegdkmodule;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class GdkModuleModule extends ReactContextBaseJavaModule {
  public static final String NAME = GdkModuleModuleImpl.NAME;

  GdkModuleModule(ReactApplicationContext context) {
    super(context);
  }

  @Override
  @NonNull
  public String getName() {
    return GdkModuleModuleImpl.NAME;
  }

  @Override
  @ReactMethod
  public void generateMnemonic12(Promise promise) {
    GdkModuleModuleImpl.generateMnemonic12(promise);
  }

  @Override
  @ReactMethod
  public void validateMnemonic(String mnemonic, Promise promise) {
    GdkModuleModuleImpl.validateMnemonic(mnemonic, promise);
  }

  @Override
  @ReactMethod
  public void gdkInit(Promise promise) {
    GdkModuleModuleImpl.gdkInit(promise);
  }

  @Override
  @ReactMethod
  public void createSession(Promise promise) {
    GdkModuleModuleImpl.createSession(promise);
  }

  @Override
  @ReactMethod
  public void connect(String sessionId, ReadableMap netParams, Promise promise) {
    GdkModuleModuleImpl.connect(sessionId, netParams, promise);
  }

  @Override
  @ReactMethod
  public void login(String sessionId, String mnemonic, Promise promise) {
    GdkModuleModuleImpl.login(sessionId, mnemonic, promise);
  }

  @Override
  @ReactMethod
  public void getReceiveAddress(String sessionId, ReadableMap details, Promise promise) {
    GdkModuleModuleImpl.getReceiveAddress(sessionId, details, promise);
  }

  @Override
  @ReactMethod
  public void getNetworks(Promise promise) {
    GdkModuleModuleImpl.getNetworks(promise);
  }

  @Override
  @ReactMethod
  public void registerNetwork(String name, ReadableMap details, Promise promise) {
    GdkModuleModuleImpl.registerNetwork(name, details, promise);
  }

  @Override
  @ReactMethod
  public void getTransactions(String sessionId, ReadableMap details, Promise promise) {
    GdkModuleModuleImpl.getTransactions(sessionId, details, promise);
  }

  @Override
  @ReactMethod
  public void refresh(String sessionId, Promise promise) {
    GdkModuleModuleImpl.refresh(sessionId, promise);
  }

  @Override
  @ReactMethod
  public void getFeeEstimates(String sessionId, Promise promise) {
    GdkModuleModuleImpl.getFeeEstimates(sessionId, promise);
  }

  @Override
  @ReactMethod
  public void getBalance(String sessionId, ReadableMap details, Promise promise) {
    GdkModuleModuleImpl.getBalance(sessionId, details, promise);
  }

  @Override
  @ReactMethod
  public void getSubaccounts(String sessionId, ReadableMap details, Promise promise) {
    GdkModuleModuleImpl.getSubaccounts(sessionId, details, promise);
  }

  @Override
  @ReactMethod
  public void createTransaction(String sessionId, ReadableMap details, Promise promise) {
    GdkModuleModuleImpl.createTransaction(sessionId, details, promise);
  }

  @Override
  @ReactMethod
  public void sendTransaction(String sessionId, ReadableMap details, Promise promise) {
    GdkModuleModuleImpl.sendTransaction(sessionId, details, promise);
  }

  @Override
  @ReactMethod
  public void getUnspentOutputs(String sessionId, ReadableMap details, Promise promise) {
    GdkModuleModuleImpl.getUnspentOutputs(sessionId, details, promise);
  }

  @Override
  @ReactMethod
  public void setPin(String sessionId, String mnemonic, String pin, Promise promise) {
    GdkModuleModuleImpl.setPin(sessionId, mnemonic, pin, promise);
  }

  @Override
  @ReactMethod
  public void loginWithPin(String sessionId, String pin, ReadableMap pin_data, Promise promise) {
    GdkModuleModuleImpl.loginWithPin(sessionId, pin, pin_data, promise);
  }

  @Override
  @ReactMethod
  public void signTransaction(String sessionId, ReadableMap details, Promise promise) {
    GdkModuleModuleImpl.signTransaction(sessionId, details, promise);
  }

  @Override
  @ReactMethod
  public void broadcastTransaction(String sessionId, String transactionHex, Promise promise) {
    GdkModuleModuleImpl.broadcastTransaction(sessionId, transactionHex, promise);
  }

  @Override
  @ReactMethod
  public void createSubaccount(String sessionId, ReadableMap details, Promise promise) {
    GdkModuleModuleImpl.createSubaccount(sessionId, details, promise);
  }

  @Override
  @ReactMethod
  public void getSubaccount(String sessionId, double subaccount, Promise promise) {
    GdkModuleModuleImpl.getSubaccount(sessionId, subaccount, promise);
  }

  @Override
  @ReactMethod
  public void getMnemonic(String sessionId, Promise promise) {
    GdkModuleModuleImpl.getMnemonic(sessionId, promise);
  }

  @Override
  @ReactMethod
  public void loginUser(String sessionId, ReadableMap hwDevice, ReadableMap details, Promise promise) {
    GdkModuleModuleImpl.loginUser(sessionId, hwDevice, details, promise);
  }

  @Override
  @ReactMethod
  public void registerUser(String sessionId, ReadableMap hwDevice, String mnemonic, Promise promise) {
    GdkModuleModuleImpl.registerUser(sessionId, hwDevice, mnemonic, promise);
  }

  @Override
  @ReactMethod
  public void getTransactionDetails(String sessionId, String txHash, Promise promise) {
    GdkModuleModuleImpl.getTransactionDetails(sessionId, txHash, promise);
  }
}
