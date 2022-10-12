import Foundation
import ga.sdk
import OSLog

let _generateMnemonic12 = generateMnemonic12
let _validateMnemonic12 = validateMnemonic
let _getNetworks = getNetworks
let _registerNetwork = registerNetwork
let _gdkInit = gdkInit

enum SessionError: Error {
  case sessionDoesNotExist
}

extension SessionError: LocalizedError {
  public var errorDescription: String? {
    switch self {
    case .sessionDoesNotExist:
      return NSLocalizedString(
        "The provided session does not exist.",
        comment: "Session doesn't exist."
      )
    }
  }
}

func randomString(length: Int) -> String {
  let letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
  return String((0..<length).map{ _ in letters.randomElement()! })
}

@objc(GdkModule)
class GdkModule: RCTEventEmitter {
  var activeNetworkName: String = "liquid-electrum-mainnet"
  var sessions: [String: Session?] = [:]
  let log = OSLog.init(subsystem: "com.sevenlabs.xdex", category: "GDK_MODULE")

  var network: Network {
      let networks = try! _getNetworks()
      let networkDetails = networks![activeNetworkName] as? [String: Any]
      let jsonData = try! JSONSerialization.data(withJSONObject: networkDetails!)
      return try! JSONDecoder().decode(Network.self, from: jsonData)
  }

  func sessionGuard (_ sessionId: String) throws -> Void {
    guard (self.sessions[sessionId] != nil) else {
      throw SessionError.sessionDoesNotExist
    }
  }



  @objc(generateMnemonic12:rejecter:)
  func generateMnemonic12(_ resolve: RCTPromiseResolveBlock, rejecter reject:RCTPromiseRejectBlock) -> Void {
    logWrapper(log, type: .default, args: [])
    do {
      try resolve(_generateMnemonic12())
    } catch let error {
      handleReject(error, log: log, rejecter: reject)
    }
  }

  @objc(validateMnemonic:resolver:rejecter:)
  func validateMnemonic(_ mnemonic: String, resolver resolve: RCTPromiseResolveBlock, rejecter reject:RCTPromiseRejectBlock) -> Void {
    do {
      logWrapper(log, type: .default, args: [mnemonic])
      try resolve(_validateMnemonic12(mnemonic))
    } catch let error {
      handleReject(error, log: log, rejecter: reject)
    }
  }

  @objc(gdkInit:rejecter:)
  func gdkInit(_ resolve: RCTPromiseResolveBlock, rejecter reject:RCTPromiseRejectBlock) -> Void {
    do {
      logWrapper(log, type: .default, args: [])
      self.sessions = [:]
      let url = try FileManager.default.url(for: .applicationSupportDirectory, in: .userDomainMask, appropriateFor: nil, create: true).appendingPathComponent(Bundle.main.bundleIdentifier!, isDirectory: true)
      try FileManager.default.createDirectory(atPath: url.path, withIntermediateDirectories: true, attributes: nil)
      try _gdkInit(["datadir": url.path])
      resolve(true)
    } catch let error {
      handleReject(error, log: log, rejecter: reject)
    }
  }

  @objc(createSession:rejecter:)
  func createSession(_ resolve: RCTPromiseResolveBlock, rejecter reject:RCTPromiseRejectBlock) -> Void {
    do {
      logWrapper(log, type: .default, args: [])
      let id = randomString(length: 8)
      let session = try Session()
      session.setNotificationHandler(notificationCompletionHandler: newNotification)
      self.sessions[id] = session
      resolve(id)
    } catch let error {
      handleReject(error, log: log, rejecter: reject)
    }
  }

  @objc(connect:netParams:resolver:rejecter:)
  func connect(_ sessionId: String, netParams: [String: Any], resolver resolve: RCTPromiseResolveBlock, rejecter reject:RCTPromiseRejectBlock) -> Void {
    do {
      logWrapper(log, type: .default, args: [sessionId, netParams])
      try sessionGuard(sessionId)
      try self.sessions[sessionId]??.connect(netParams: netParams)
      activeNetworkName = netParams["name"] as! String
      sendEvent(withName: "connected", body: ["networkName": activeNetworkName])
      resolve(true)
    } catch let error {
      handleReject(error, log: log, rejecter: reject)
    }
  }

  @objc(getReceiveAddress:details:resolver:rejecter:)
  public func getReceiveAddress(_ sessionId: String, details: [String: Any], resolver resolve: RCTPromiseResolveBlock, rejecter reject:RCTPromiseRejectBlock) -> Void {
    do {
      logWrapper(log, type: .default, args: [sessionId, details])
      try sessionGuard(sessionId)
      let call = try? self.sessions[sessionId]??.getReceiveAddress(details: details)
      let data = try? DummyResolve(call: call!)
      let result = data?["result"] as? [String: Any]
      resolve(result?["address"] as? String)
    } catch let error {
      handleReject(error, log: log, rejecter: reject)
    }
  }

  @objc(getNetworks:rejecter:)
  public func getNetworks(_ resolve: RCTPromiseResolveBlock, rejecter reject:RCTPromiseRejectBlock) -> Void {
    do {
      logWrapper(log, type: .default, args: [])
      let networks = try _getNetworks()
      resolve(networks)
    } catch {
      handleReject(error, log: log, rejecter: reject)
    }
  }

  @objc(registerNetwork:details:resolver:rejecter:)
  func registerNetwork(_ name: String, details: [String: Any], resolver resolve: RCTPromiseResolveBlock, rejecter reject:RCTPromiseRejectBlock) -> Void {
    logWrapper(log, type: .default, args: [])
    try! _registerNetwork(name, details)
    resolve(true)
  }

  @objc(getTransactions:details:resolver:rejecter:)
  func getTransactions(_ sessionId: String, details: [String: Any], resolver resolve: RCTPromiseResolveBlock, rejecter reject:RCTPromiseRejectBlock) -> Void {
    do {
      logWrapper(log, type: .default, args: [sessionId, details])
      try sessionGuard(sessionId)
      let call = try self.sessions[sessionId]??.getTransactions(details: details)
      let data = try DummyResolve(call: call!)
      let result = data["result"] as? [String: Any]
      let list = result?["transactions"] as? [[String: Any]]
      resolve(list)
    } catch let error {
      handleReject(error, log: log, rejecter: reject)
    }
  }

  @objc(refresh:resolver:rejecter:)
  func refresh(_ sessionId: String, resolver resolve: RCTPromiseResolveBlock, rejecter reject:RCTPromiseRejectBlock) -> Void {
    do {
      logWrapper(log, type: .default, args: [sessionId])
      try sessionGuard(sessionId)
      let data = try self.sessions[sessionId]??.refreshAssets(params: ["icons": true, "assets": true, "refresh": true])
      resolve(data)
    } catch let error {
      handleReject(error, log: log, rejecter: reject)
    }
  }

  @objc(getFeeEstimates:resolver:rejecter:)
  func getFeeEstimates(_ sessionId: String, resolver resolve: RCTPromiseResolveBlock, rejecter reject:RCTPromiseRejectBlock) -> Void {
    do {
      logWrapper(log, type: .default, args: [sessionId])
      try sessionGuard(sessionId)
      let fees = try self.sessions[sessionId]??.getFeeEstimates()?["fees"] as? [UInt64]
      resolve([
        "default": fees?.last ?? 1000,
        "fast": fees?[1] ?? 1000
      ])
    } catch let error {
      handleReject(error, log: log, rejecter: reject)
    }
  }

  @objc(getBalance:details:resolver:rejecter:)
  func getBalance(_ sessionId: String, details: [String: Any], resolve: RCTPromiseResolveBlock, rejecter reject:RCTPromiseRejectBlock) -> Void {
    do {
      logWrapper(log, type: .default, args: [sessionId, details])
      try sessionGuard(sessionId)
      let call = try self.sessions[sessionId]??.getBalance(details: details)
      let data = try DummyResolve(call: call!)
      let result = data["result"] as? [String: Any]
      if (result?["error"] != nil) {
        handleReject(NSError(domain: "GET_BALANCE_ERROR", code: 2, userInfo: nil), log: log, rejecter: reject)
      } else {
        resolve(result)
      }
    } catch let error {
      handleReject(error, log: log, rejecter: reject)
    }
  }

  @objc(getSubaccounts:details:resolver:rejecter:)
  func getSubaccounts(_  sessionId: String, details: [String: Any],  resolver resolve: RCTPromiseResolveBlock, rejecter reject:RCTPromiseRejectBlock) -> Void {
    do {
      logWrapper(log, type: .default, args: [sessionId, details])
      try sessionGuard(sessionId)
      let call = try self.sessions[sessionId]??.getSubaccounts(details: details)
      let data = try DummyResolve(call: call!)
      let result = data["result"] as? [String: Any]
      if (result?["error"] != nil) {
        handleReject(NSError(domain: "GET_SUBACCOUNT_ERROR", code: 2, userInfo: nil), log: log, rejecter: reject)
      } else {
        resolve(result?["subaccounts"])
      }
    } catch let error {
      handleReject(error, log: log, rejecter: reject)
    }
  }

  @objc(createTransaction:details:resolver:rejecter:)
  func createTransaction(_ sessionId: String, details: [String: Any], resolver resolve: RCTPromiseResolveBlock, rejecter reject:RCTPromiseRejectBlock) -> Void {
    do {
      logWrapper(log, type: .default, args: [sessionId, details])
      try sessionGuard(sessionId)
      let call = try self.sessions[sessionId]!!.createTransaction(details: details)
      let data = try DummyResolve(call: call)
      let result = data["result"] as? [String: Any]
      if let error = result?["error"] as? String, error != "" {
        handleReject(TransactionError.generic(error), log: log, rejecter: reject)
        return
      }
      resolve(result)
    } catch let error {
      handleReject(error, log: log, rejecter: reject)
    }
  }

  @objc(signTransaction:details:resolver:rejecter:)
  func signTransaction(_ sessionId: String, details: [String: Any], resolver resolve: RCTPromiseResolveBlock, rejecter reject:RCTPromiseRejectBlock) -> Void {
    do {
      logWrapper(log, type: .default, args: [sessionId, details])
      try sessionGuard(sessionId)
      let call = try self.sessions[sessionId]!!.signTransaction(details: details)
      let data = try DummyResolve(call: call)
      let result = data["result"] as? [String: Any]
      if let error = result?["error"] as? String, error != "" {
        handleReject(TransactionError.generic(error), log: log, rejecter: reject)
        return
      }
      resolve(result)
    } catch let error {
      handleReject(error, log: log, rejecter: reject)
    }
  }

  @objc(broadcastTransaction:txHex:resolver:rejecter:)
  func broadcastTransaction(_ sessionId: String, txHex: String, resolver resolve: RCTPromiseResolveBlock, rejecter reject:RCTPromiseRejectBlock) -> Void {
    do {
      logWrapper(log, type: .default, args: [sessionId, txHex])
      try sessionGuard(sessionId)
      let result = try self.sessions[sessionId]!!.broadcastTransaction(tx_hex: txHex)
      resolve(result)
    } catch let error {
      handleReject(error, log: log, rejecter: reject)
    }
  }

  @objc(sendTransaction:details:resolver:rejecter:)
  func sendTransaction(_ sessionId: String, details: [String: Any], resolver resolve: RCTPromiseResolveBlock, rejecter reject:RCTPromiseRejectBlock) -> Void {
    do {
      logWrapper(log, type: .default, args: [sessionId, details])
      try sessionGuard(sessionId)
      let call = try self.sessions[sessionId]!!.sendTransaction(details: details)
      let data = try DummyResolve(call: call)
      let result = data["result"] as? [String: Any]
      if let error = result?["error"] as? String, error != "" {
        handleReject(TransactionError.generic(error), log: log, rejecter: reject)
        return
      }
      resolve(result)
    } catch let error {
      handleReject(error, log: log, rejecter: reject)
    }
  }

  @objc(getUnspentOutputs:details:resolver:rejecter:)
  func getUnspentOutputs(_ sessionId: String, details: [String: Any], resolver resolve: RCTPromiseResolveBlock, rejecter reject:RCTPromiseRejectBlock) -> Void {
    do {
      logWrapper(log, type: .default, args: [sessionId, details])
      try sessionGuard(sessionId)
      let call = try self.sessions[sessionId]??.getUnspentOutputs(details: details)
      let data = try DummyResolve(call: call!)
      let result = data["result"] as? [String: Any]
      resolve(result?["unspent_outputs"] as? [String: Any])
    } catch let error {
      handleReject(error, log: log, rejecter: reject)
    }
  }

  @objc(setPin:mnemonic:pin:resolver:rejecter:)
  func setPin(_ sessionId: String, mnemonic: String, pin: String, resolver resolve: RCTPromiseResolveBlock, rejecter reject:RCTPromiseRejectBlock) -> Void {
    do {
      logWrapper(log, type: .default, args: [sessionId, mnemonic, pin])
      try sessionGuard(sessionId)
      let result = try self.sessions[sessionId]??.setPin(mnemonic: mnemonic, pin: pin, device: "")
      resolve(result)
    } catch let error {
      handleReject(error, log: log, rejecter: reject)
    }
  }

  @objc(createSubaccount:details:resolver:rejecter:)
  func createSubaccount(_ sessionId: String, details: [String: Any], resolve: RCTPromiseResolveBlock, rejecter reject:RCTPromiseRejectBlock) -> Void {
    do {
      logWrapper(log, type: .default, args: [sessionId, details])
      try sessionGuard(sessionId)
      let call = try self.sessions[sessionId]??.createSubaccount(details: details)
      let data = try DummyResolve(call: call!)
      let result = data["result"] as? [String: Any]
      if (result?["error"] != nil) {
        handleReject(NSError(domain: "\(String(describing: result?["error"]))", code: 2, userInfo: nil), log: log, rejecter: reject)
      } else {
        resolve(result)
      }
    } catch let error {
      handleReject(error, log: log, rejecter: reject)
    }
  }

  @objc(getSubaccount:subaccount:resolver:rejecter:)
  func getSubaccount(_ sessionId: String, subaccount: NSInteger, resolver resolve: RCTPromiseResolveBlock, rejecter reject:RCTPromiseRejectBlock) -> Void {
    do {
      logWrapper(log, type: .default, args: [sessionId, subaccount])
      try sessionGuard(sessionId)
      let call = try self.sessions[sessionId]??.getSubaccount(subaccount: UInt32(subaccount))
      let data = try DummyResolve(call: call!)
      let result = data["result"] as? [String: Any]
      if (result?["error"] != nil) {
        handleReject(NSError(domain: "\(String(describing: result?["error"]))", code: 2, userInfo: nil), log: log, rejecter: reject)
      } else {
        resolve(result)
      }
    } catch let error {
      handleReject(error, log: log, rejecter: reject)
    }
  }

  @objc(getMnemonic:resolver:rejecter:)
  func getMnemonic(_ sessionId: String, resolver resolve: RCTPromiseResolveBlock, rejecter reject:RCTPromiseRejectBlock) -> Void {
    do {
      logWrapper(log, type: .default, args: [sessionId])
      try sessionGuard(sessionId)
      resolve(try self.sessions[sessionId]??.getMnemonicPassphrase(password: ""))
    } catch let error {
      handleReject(error, log: log, rejecter: reject)
    }
  }

  @objc(loginUser:hw_device:details:resolver:rejecter:)
  func loginUser(_ sessionId: String, hw_device: [String: Any] = [:], details: [String: Any], resolver resolve: RCTPromiseResolveBlock, rejecter reject:RCTPromiseRejectBlock) -> Void {
    do {
      logWrapper(log, type: .default, args: [sessionId, hw_device, details])
      try sessionGuard(sessionId)
      let call = try self.sessions[sessionId]??.loginUser(details: details, hw_device: hw_device)
      _ = try DummyResolve(call: call!)
      resolve(true)
    } catch let error {
      handleReject(error, log: log, rejecter: reject)
    }
  }

  @objc(registerUser:hw_device:mnemonic:resolver:rejecter:)
  func registerUser(_ sessionId: String, hw_device: [String: Any] = [:], mnemonic: String,  resolver resolve: RCTPromiseResolveBlock, rejecter reject:RCTPromiseRejectBlock) -> Void {
    do {
      logWrapper(log, type: .default, args: [sessionId, hw_device, mnemonic])
      try sessionGuard(sessionId)
      let call = try self.sessions[sessionId]??.registerUser(mnemonic: mnemonic, hw_device: hw_device)
      _ = try DummyResolve(call: call!)
      resolve(true)
    } catch let error {
      handleReject(error, log: log, rejecter: reject)
    }
  }

  @objc(getTransactionDetails:txHash:resolver:rejecter:)
  func getTransactionDetails(_ sessionId: String, txHash: String, resolver resolve: RCTPromiseResolveBlock, rejecter reject:RCTPromiseRejectBlock) -> Void {
    do {
      logWrapper(log, type: .default, args: [sessionId, txHash])
      try sessionGuard(sessionId)
      let result = try self.sessions[sessionId]??.getTransactionDetails(txhash: txHash)
      resolve(result)
    } catch let error {
      handleReject(error, log: log, rejecter: reject)
    }
  }

  func newNotification(notification: [String: Any]?) -> Void {
    sendEvent(withName: notification?["event"] as? String, body: notification)
  }

  @objc
  open override func supportedEvents() -> [String]! {
    return ["connected", "block", "fees", "settings", "tor", "transaction", "twofactor_reset", "ticker", "network", "session"]
  }

  @objc
  override static func requiresMainQueueSetup() -> Bool {
    return false
  }
}
