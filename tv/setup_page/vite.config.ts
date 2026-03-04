import {defineConfig} from "vite";
import {viteSingleFile} from "vite-plugin-singlefile";
import htmlMinifier from "vite-plugin-html-minifier";

export default defineConfig({
    plugins: [
        viteSingleFile(),
        htmlMinifier({
            minify: true
        })],

    esbuild: {
        legalComments: "none",
        supported: {
            "top-level-await": true // browsers can handle top-level-await features
        }
    },
    server: {
        host: true,
        port: 3000,
        strictPort: true,
        hmr: {
            protocol: "ws",
            host: "localhost",
            port: 3000,
            clientPort: 3000,
            overlay: true
        }
    },
    build: {
        outDir: "../src/main/res/raw",
        rollupOptions: {
            output: {
                entryFileNames: "setup_page.js",
                assetFileNames: "setup_page.[ext]"
            }
        }
    }
});