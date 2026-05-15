import { useState } from "react";
import { signUp } from "aws-amplify/auth";
import { useNavigate } from "@tanstack/react-router";
export default function RegisterPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();
  const handleRegister = async (e) => {
    e.preventDefault();
    setError("");
    try {
      await signUp({
        username: email,
        password,
        options: {
          userAttributes: { email },
        },
      });
      // Cognito skickar en verifieringskod till e-posten
      navigate({ to: "/confirm", search: { email } });
    } catch (err) {
      setError(err.message);
    }
  };
  return (
    <div>
      <h1>Registrera konto</h1>
      <form onSubmit={handleRegister}>
        <input
          type="email"
          placeholder="E-post"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        <input
          type="password"
          placeholder="Lösenord (minst 8 tecken)"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        {error && <p style={{ color: "red" }}>{error}</p>}
        <button type="submit">Registrera</button>
      </form>
      <a href="/login">Har du redan ett konto? Logga in</a>
    </div>
  );
}
