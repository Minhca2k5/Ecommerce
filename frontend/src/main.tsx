import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./index.css";
import App from "./App";
import { AuthProvider } from "@/app/AuthProvider";
import { ToastProvider } from "@/app/ToastProvider";
import { NotificationProvider } from "@/app/NotificationProvider";
import { ThemeProvider } from "@/app/ThemeProvider";

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <ToastProvider>
      <ThemeProvider>
        <AuthProvider>
          <NotificationProvider>
            <App />
          </NotificationProvider>
        </AuthProvider>
      </ThemeProvider>
    </ToastProvider>
  </StrictMode>
);


