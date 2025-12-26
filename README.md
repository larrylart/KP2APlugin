## Experimental KP2A / KeePassDX AIDL Integration

This release introduces an **experimental integration** that modifies the **KP2A Plugin** to expose an **AIDL service**, and a corresponding **KeePassDX fork** that consumes this service to communicate with HID providers via AIDL.

⚠️ **Experimental / unverified end-to-end**

- I do **not** own an InputStick device, so full end-to-end testing has **not** been possible.
- Verification so far is limited to:
  - The plugin being correctly **discovered** by the KeePassDX AIDL client.
  - Successful **string exchange** between KeePassDX and the plugin via AIDL.

There is no guarantee that the complete flow (KeePassDX → plugin → HID device) works on real hardware at this stage.

### Requirements

To use this integration, you must install and use the following modified KeePassDX fork:

- **KeePassDX fork with AIDL support**  
  https://github.com/larrylart/KeePassDX

Further testing and feedback are welcome as this integration is iterated on.

---
# KP2A InputStick Plugin
KP2A plugin allows to use InputStick USB receiver to type (as USB keyboard) data stored in Keepass2Android (KP2A) password manager.

[User's manual pdf](https://docs.google.com/uc?id=0B2RufT7QrvYGNE5rbWFRX1k0R28&export=download)

[.apk download](http://inputstick.com/download/)

## More info:
[Visit inputstick.com](http://inputstick.com)
