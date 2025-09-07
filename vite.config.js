import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";
import { VitePWA } from "vite-plugin-pwa";

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "");
  const enablePWA = process.env.BUILD_PWA === "true";
  const proxyTarget = env.VITE_API_BASE_URL || process.env.PROXY_TARGET;
  return {
    server: {
      host: "127.0.0.1",
      port: Number(process.env.PORT) || 5174,
      strictPort: false,
      proxy: proxyTarget
        ? {
            "/api": {
              target: proxyTarget,
              changeOrigin: true,
              secure: false,
            },
            "/ws": {
              target: proxyTarget,
              ws: true,
              changeOrigin: true,
              secure: false,
            },
          }
        : undefined,
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
