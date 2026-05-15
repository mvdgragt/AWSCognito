import {
  createRouter,
  createRoute,
  createRootRoute,
  Outlet,
  redirect,
} from "@tanstack/react-router";
import { getCurrentUser } from "aws-amplify/auth";
import HomePage from "./pages/HomePage";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import DashboardPage from "./pages/DashboardPage";
import ConfirmPage from "./pages/ConfirmPage";

// Root route — renderar alla sidor via <Outlet />
const rootRoute = createRootRoute({
  component: () => <Outlet />,
});

// Hjälpfunktion: är användaren inloggad?
async function requireAuth() {
  try {
    await getCurrentUser();
  } catch {
    throw redirect({ to: "/login" });
  }
}
const indexRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/",
  component: HomePage,
});
const loginRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/login",
  component: LoginPage,
});
const registerRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/register",
  component: RegisterPage,
});
const confirmRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/confirm",
  component: ConfirmPage,
});

// Skyddad route — beforeLoad körs INNAN komponenten renderas
const dashboardRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/dashboard",
  beforeLoad: requireAuth,
  component: DashboardPage,
});
const routeTree = rootRoute.addChildren([
  indexRoute,
  loginRoute,
  registerRoute,
  confirmRoute,
  dashboardRoute,
]);

export const router = createRouter({ routeTree });
