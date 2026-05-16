import { useNavigate } from "@tanstack/react-router";

export default function HomePage() {
  const navigate = useNavigate();

  return (
    <div>
      <h1>Välkommen till Todos App</h1>
      <p>Logga in eller registrera ett konto för att komma igång.</p>
      <button onClick={() => navigate({ to: "/login" })}>Logga in</button>
      <button onClick={() => navigate({ to: "/register" })}>Registrera</button>
    </div>
  );
}
