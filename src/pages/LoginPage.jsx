import { useState } from "react";
import { signIn } from "aws-amplify/auth";
import { useNavigate } from "@tanstack/react-router";
export default function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();
  const handleLogin = async (e) => {
    e.preventDefault();
    setError("");
    try {
      await signIn({ username: email, password });
      navigate({ to: "/dashboard" });
    } catch (err) {
      setError(err.message);
    }
  };
  return (
    <div>
      <h1>Logga in</h1>
      <form onSubmit={handleLogin}>
        <input
          type="email"
          placeholder="E-post"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        <input
          type="password"
          placeholder="Lösenord"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        {error && <p style={{ color: "red" }}>{error}</p>}
        <button type="submit">Logga in</button>
      </form>
      <a href="/register">Inget konto? Registrera dig</a>
    </div>
  );
}
