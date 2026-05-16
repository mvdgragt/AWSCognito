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
      navigate({ to: "/confirm", search: { email } });
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-8 w-full max-w-sm">
        <h1 className="text-xl font-semibold text-gray-900 mb-6">Registrera konto</h1>
        <form onSubmit={handleRegister} className="flex flex-col gap-3">
          <input
            type="email"
            placeholder="E-post"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            className="w-full px-3 py-2.5 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-gray-200"
          />
          <input
            type="password"
            placeholder="Lösenord (minst 8 tecken)"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            className="w-full px-3 py-2.5 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-gray-200"
          />
          {error && <p className="text-red-500 text-sm">{error}</p>}
          <button
            type="submit"
            className="w-full bg-gray-900 text-white py-2.5 rounded-lg text-sm font-medium hover:bg-gray-700 transition-colors mt-1"
          >
            Registrera
          </button>
        </form>
        <p className="text-center text-sm text-gray-400 mt-5">
          Har du redan ett konto?{" "}
          <a href="/login" className="text-gray-900 font-medium hover:underline">
            Logga in
          </a>
        </p>
      </div>
    </div>
  );
}
