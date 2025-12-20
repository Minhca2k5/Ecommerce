import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./index.css";
import App from "./App";
import { AuthProvider } from "@/app/AuthProvider";
import { ToastProvider } from "@/app/ToastProvider";
import { NotificationProvider } from "@/app/NotificationProvider";

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <ToastProvider>
      <AuthProvider>
        <NotificationProvider>
          <App />
        </NotificationProvider>
      </AuthProvider>
    </ToastProvider>
  </StrictMode>
);


