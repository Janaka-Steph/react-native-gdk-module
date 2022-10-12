import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-gdk-module' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo managed workflow\n';

// @ts-expect-error
const isTurboModuleEnabled = global.__turboModuleProxy != null;

const GdkModuleModule = isTurboModuleEnabled
  ? require('./NativeGdkModule').default
  : NativeModules.GdkModule;

const GdkModule = GdkModuleModule
  ? GdkModuleModule
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export function generateMnemonic12(): Promise<string> {
  return GdkModule.generateMnemonic12();
}
