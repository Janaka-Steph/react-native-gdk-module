import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-gdk-module-turbocompat' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo managed workflow\n';

// @ts-expect-error
const isTurboModuleEnabled = global.__turboModuleProxy != null;

const GdkModuleTurbocompatModule = isTurboModuleEnabled
  ? require('./NativeGdkModuleTurbocompat').default
  : NativeModules.GdkModuleTurbocompat;

const GdkModuleTurbocompat = GdkModuleTurbocompatModule
  ? GdkModuleTurbocompatModule
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export function multiply(a: number, b: number): Promise<number> {
  return GdkModuleTurbocompat.multiply(a, b);
}
