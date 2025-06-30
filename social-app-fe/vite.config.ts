import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from "path"
import vueDevtools from 'vite-plugin-vue-devtools'


export default defineConfig({
  plugins: [
    vue(),
    vueDevtools(),
  ],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
})