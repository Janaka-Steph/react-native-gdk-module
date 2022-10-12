package com.reactnativegdkmoduleturbocompat;

import androidx.annotation.NonNull;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactMethod;

public class GdkModuleTurbocompatModule extends NativeGdkModuleTurbocompatSpec {
  public static final String NAME = GdkModuleTurbocompatModuleImpl.NAME;

  GdkModuleTurbocompatModule(ReactApplicationContext context) {
    super(context);
  }

  @Override
  @NonNull
  public String getName() {
    return GdkModuleTurbocompatModuleImpl.NAME;
  }

  // Example method
  // See https://reactnative.dev/docs/native-modules-android
  @Override
  @ReactMethod
  public void multiply(double a, double b, Promise promise) {
    GdkModuleTurbocompatModuleImpl.multiply(a, b, promise);
  }
}
