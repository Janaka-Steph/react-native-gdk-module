#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface RCT_EXTERN_MODULE(GdkModule, RCTEventEmitter)

RCT_EXTERN_METHOD(gdkInit:
                  (RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
)

RCT_EXTERN_METHOD(generateMnemonic12:
                  (RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
)

RCT_EXTERN_METHOD(validateMnemonic:(NSString *)mnemonic
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
)

RCT_EXTERN_METHOD(createSession:
                  (RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
)

RCT_EXTERN_METHOD(connect:(NSString *)sessionId
                  netParams:(NSDictionary *)netParams
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
)

RCT_EXTERN_METHOD(getReceiveAddress:(NSString *)sessionId
                  details:(NSDictionary *)details
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
)

RCT_EXTERN_METHOD(getNetworks:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
)

RCT_EXTERN_METHOD(registerNetwork:(NSString *)name
                  details:(NSDictionary *)details
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
)

RCT_EXTERN_METHOD(getTransactions:(NSString *)sessionId
                  details:(NSDictionary *)details
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
)

RCT_EXTERN_METHOD(refresh:(NSString *)sessionId
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
)

RCT_EXTERN_METHOD(getFeeEstimates:(NSString *)sessionId
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
)

RCT_EXTERN_METHOD(getBalance:(NSString *)sessionId
                  details:(NSDictionary *)details
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
)

RCT_EXTERN_METHOD(getSubaccounts:(NSString *)sessionId
                  details:(NSDictionary *)details
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
)

RCT_EXTERN_METHOD(createTransaction:(NSString *)sessionId
                  details:(NSDictionary *)details
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
)

RCT_EXTERN_METHOD(signTransaction:(NSString *)sessionId
                  details:(NSDictionary *)details
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
)

RCT_EXTERN_METHOD(sendTransaction:(NSString *)sessionId
                  details:(NSDictionary *)details
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
)

RCT_EXTERN_METHOD(broadcastTransaction:(NSString *)sessionId
                  txHex:(NSString *)txHex
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
)

RCT_EXTERN_METHOD(getUnspentOutputs:(NSString *)sessionId
                  details:(NSDictionary *)details
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
)

RCT_EXTERN_METHOD(setPin:(NSString *)sessionId
                  mnemonic:(NSString *)mnemonic
                  pin:(NSString *)pin
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
)

RCT_EXTERN_METHOD(createSubaccount:(NSString *)sessionId
                  details:(NSDictionary *)details
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
)

RCT_EXTERN_METHOD(getSubaccount:(NSString *)sessionId
                  subaccount:(NSInteger)subaccount
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
)

RCT_EXTERN_METHOD(getMnemonic:(NSString *)sessionId
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
)

RCT_EXTERN_METHOD(loginUser:(NSString *)sessionId
                  hw_device:(NSDictionary *)hw_device
                  details:(NSDictionary *)details
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
)

RCT_EXTERN_METHOD(registerUser:(NSString *)sessionId
                  hw_device:(NSDictionary *)hw_device
                  mnemonic:(NSString *)mnemonic
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
)

RCT_EXTERN_METHOD(getTransactionDetails:(NSString *)sessionId
                  txHash:(NSString *)txHash
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
)


@end
