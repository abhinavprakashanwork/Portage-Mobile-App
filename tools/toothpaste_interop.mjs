import assert from "node:assert/strict";
import { webcrypto } from "node:crypto";

const hex = (value) => Uint8Array.from(value.match(/../g), (pair) => parseInt(pair, 16));
const sharedSecret = hex("000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f");
const salt = hex("a0a1a2a3a4a5a6a7a8a9aaabacadaeaf");
const info = new TextEncoder().encode("aes-gcm-256");
const keyMaterial = await webcrypto.subtle.importKey("raw", sharedSecret, "HKDF", false, ["deriveKey"]);
const aesKey = await webcrypto.subtle.deriveKey(
  { name: "HKDF", hash: "SHA-256", salt, info },
  keyMaterial,
  { name: "AES-GCM", length: 256 },
  true,
  ["encrypt", "decrypt"]
);
const keyBytes = new Uint8Array(await webcrypto.subtle.exportKey("raw", aesKey));
const iv = hex("1af38c2dc2b96ffdd8669409");
const plaintext = new TextEncoder().encode("ToothPaste interoperability");
const combined = new Uint8Array(await webcrypto.subtle.encrypt({ name: "AES-GCM", iv }, aesKey, plaintext));
const decrypted = new Uint8Array(await webcrypto.subtle.decrypt({ name: "AES-GCM", iv }, aesKey, combined));
assert.deepEqual(decrypted, plaintext);

const authKey = new TextEncoder().encode("AgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=");
const authPacket = new Uint8Array([
  0x08, 0x01, 0x10, 0x01, 0x20, 0x01,
  0x2a, 0x0c, ...new Uint8Array(12),
  0x30, authKey.length, 0x3a, authKey.length, ...authKey,
  0x42, 0x10, ...new Uint8Array(16),
]);
assert.equal(authPacket.length, 86);
console.log(JSON.stringify({
  hkdfKey: Buffer.from(keyBytes).toString("hex"),
  ciphertextAndTag: Buffer.from(combined).toString("hex"),
  authPacket: Buffer.from(authPacket).toString("hex"),
}));
