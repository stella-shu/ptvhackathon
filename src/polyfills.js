// src/polyfills.js
// Shim Node globals so sockjs-client (and similar libs) don't crash in browser

window.global = window;
window.process = window.process || { env: {} };

// If you later see "Buffer is not defined", you can also add:
// import { Buffer } from "buffer";
// window.Buffer = Buffer;
