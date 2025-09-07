import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";
import { VitePWA } from "vite-plugin-pwa";

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "");
  const enablePWA = process.env.BUILD_PWA === "true";
  const proxyTarget = env.VITE_API_BASE_URL || process.env.PROXY_TARGET;
  // Optional split targets to merge multiple backends behind Vite
  const authTarget = env.VITE_AUTH_BASE_URL || env.AUTH_TARGET || process.env.AUTH_TARGET;
  const realtimeTarget = env.VITE_REALTIME_BASE_URL || env.REALTIME_TARGET || process.env.REALTIME_TARGET;
  return {
    server: {
      host: "127.0.0.1",
      port: Number(process.env.PORT) || 5174,
      strictPort: false,
      proxy: (() => {
        const proxy = {};
        if (authTarget) {
          // Auth & Logs routes
          proxy["/api/auth"] = { target: authTarget, changeOrigin: true, secure: false };
          proxy["/api/incidents"] = { target: authTarget, changeOrigin: true, secure: false };
          proxy["/api/shifts"] = { target: authTarget, changeOrigin: true, secure: false };
        }
        if (realtimeTarget) {
          // Realtime maps routes
          proxy["/api/blitz"] = { target: realtimeTarget, changeOrigin: true, secure: false };
          proxy["/api/channels"] = { target: realtimeTarget, changeOrigin: true, secure: false };
          proxy["/api/location"] = { target: realtimeTarget, changeOrigin: true, secure: false };
          proxy["/ws"] = { target: realtimeTarget, ws: true, changeOrigin: true, secure: false };
        }
        // Fallback: single target if provided and no split targets
        if (!Object.keys(proxy).length && proxyTarget) {
          proxy["/api"] = { target: proxyTarget, changeOrigin: true, secure: false };
          proxy["/ws"] = { target: proxyTarget, ws: true, changeOrigin: true, secure: false };
        }
        return Object.keys(proxy).length ? proxy : undefined;
      })(),
    },
    plugins: [
      react(),
      ...(enablePWA
        ? [
            VitePWA({
              registerType: "autoUpdate",
              minify: false,
              manifest: {
                name: "Inspector Ops",
                short_name: "InspectorOps",
                start_url: "/",
                display: "standalone",
                background_color: "#ffffff",
                theme_color: "#2563eb",
                icons: [
                  {
                    src: "/vite.svg",
                    sizes: "any",
                    type: "image/svg+xml",
                    purpose: "any maskable",
                  },
                ],
              },
              workbox: { globPatterns: ["**/*.{js,css,html,svg,png}"] },
            }),
          ]
        : []),
    ],
  };
});
